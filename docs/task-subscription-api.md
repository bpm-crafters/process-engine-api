---
title: Task Subscription API
---

The Task Subscription API allows you to subscribe to different tasks. The process-engine-api adapter implementation
then takes care of the delivery of the tasks matching the intended subscription.

## Delivery semantics

A task subscription delivers the tasks that are available in the process engine and match the subscription.
When you create a new subscription, the adapter delivers already active matching tasks and then continues to deliver
matching task changes while the subscription is active. A subscription is therefore not limited to future task events.

If the application creates a new subscription, still-active tasks can be delivered again even if the previous application
instance already processed them. Consumers that send notifications, forward tasks, or synchronize tasks to another system
must handle repeated deliveries idempotently. Persist enough processing state outside the application process, for example
a last synchronization timestamp or a per-task processing marker. Compare this state with the adapter-provided created date
or updated date from `TaskInformation.meta`, and skip deliveries that were already handled.

```java

@Component
@Slf4J
@RequiredArgsConstructor
public class TaskSubscriber {

  private final TaskSubscriptionApi taskSubscriptionApi;
  private final Map<TaskInformation, Map<String, ?>> userTasks = new ConcurrentHashMap<>();


  @SneakyThrows
  public void subscribe() {
    taskSubscriptionApi.subscribeForTask(
      new SubscribeForTaskCmd(
        CommonRestrictions.builder().build(),
        TaskType.USER, // user tasks only
        null, // all of them
        null, // all variables
        (taskInfo, variables) -> {
          if (!userTasks.containsKey(taskInfo)) { // don't replace existing tasks
            log.info("[TASK LIST]: Received user task {} with meta {}", taskInfo.getTaskId(), taskInfo.getMeta());
            userTasks.put(taskInfo, variables);
          }
        },
        (taskId) -> {
          log.info("[TASK LIST]: Removing task {}", taskId);
          userTasks.keySet().stream().filter(info -> info.getTaskId().equals(taskId)).findFirst().ifPresent(userTasks::remove);
        }
      )
    ).get();
  }
} 


```

## Task execution observers

A `TaskExecutionObserver` watches the lifecycle of delivered tasks, e.g. for an audit log, a tracing span or MDC values.
It covers external service tasks and user tasks, independent of the engine and of the telemetry tool.
Once the adapter supports observers (see below), this includes annotation-based workers and handlers registered via `subscribeForTask` alike.

Every callback names the `TaskType`, so one observer can handle both types or filter:

| Callback | When | Thread |
|---|---|---|
| `onStart(task, taskType)` | right before the task handler is invoked; its cleanup runs after the handler returned or threw | delivering thread |
| `onOutcome(taskId, taskType, outcome)` | on a call to `ServiceTaskCompletionApi` or `UserTaskCompletionApi`, or when an exception escapes the handler | calling thread |
| `onTermination(task, taskType)` | when the adapter reports that a delivered task is gone, e.g. completed, cancelled or deleted | adapter's thread |

For user tasks, the handler only receives the task, e.g. for a task list, and the user completes it later.
Their outcomes are therefore reported on another thread than `onStart`, and only as `Completed`, `BpmnError` or `Thrown`.
When and whether `onTermination` is called depends on the adapter and the engine; it may come without a preceding `onStart`, after `onOutcome`, or never for service tasks.
The KDoc of `TaskExecutionObserver` describes the full contract.

The example opens a span and MDC values for each external service task and marks the span as failed.
It relies on `Span.current()`, so it only marks the span if the handler completes the task on the delivering thread before it returns, as `process-engine-worker` does.

```kotlin
@Component
class TracingTaskExecutionObserver(private val tracer: Tracer) : TaskExecutionObserver {

  override fun onStart(task: TaskInformation, taskType: TaskType): TaskExecutionCleanup {
    if (taskType != TaskType.EXTERNAL) {
      return TaskExecutionCleanup.NONE
    }
    val activityId = task.meta[CommonRestrictions.ACTIVITY_ID] ?: "unknown-activity"
    val span = tracer.spanBuilder("$activityId process").setNoParent().setSpanKind(SpanKind.CONSUMER).startSpan()
    val scope = span.makeCurrent()
    val mdc = MDC.putCloseable("activityId", activityId)
    return TaskExecutionCleanup {
      mdc.close()
      scope.close()
      span.end()
    }
  }

  override fun onOutcome(taskId: String, taskType: TaskType, outcome: TaskExecutionOutcome) {
    if (taskType != TaskType.EXTERNAL) {
      return
    }
    when (outcome) {
      is TaskExecutionOutcome.BpmnError -> Span.current().setStatus(StatusCode.ERROR, outcome.errorCode)
      is TaskExecutionOutcome.Failed -> Span.current().setStatus(StatusCode.ERROR, outcome.reason)
      is TaskExecutionOutcome.Thrown -> Span.current().recordException(outcome.cause).setStatus(StatusCode.ERROR)
      TaskExecutionOutcome.Completed -> Unit
    }
  }
}
```

An audit observer for user tasks only overrides what it needs:

```kotlin
@Component
class UserTaskAuditObserver : TaskExecutionObserver {

  override fun onOutcome(taskId: String, taskType: TaskType, outcome: TaskExecutionOutcome) {
    if (taskType == TaskType.USER) {
      logger.info { "User task $taskId ended with $outcome" }
    }
  }

  override fun onTermination(task: TaskInformation, taskType: TaskType) {
    if (taskType == TaskType.USER) {
      logger.info { "User task ${task.taskId} is gone" }
    }
  }
}
```

### Supporting observers in an adapter

An adapter passes the observers in notification order to `AbstractTaskSubscriptionApiImpl`, which observes the task handler and the termination handler of every subscription.
It also wraps its `ServiceTaskCompletionApi` in an `ObservedServiceTaskCompletionApi` and its `UserTaskCompletionApi` in an `ObservedUserTaskCompletionApi` with the same observers, and registers only the wrapped instances, so workers, manual handlers and task lists report their outcomes through them.
Without observers, the adapter keeps its plain completion APIs and nothing changes.
