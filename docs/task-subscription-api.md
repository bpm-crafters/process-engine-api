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

## Task execution decorators

Cross-cutting concerns such as tracing, MDC, logging, or metrics can wrap the execution of external
service tasks with a `TaskExecutionDecorator`, in the spirit of Spring's `TaskDecorator`. A decorator
applies to both registration paths (`@ProcessEngineWorker` and manual `subscribeForTask`). It is not an
engine listener such as a Camunda `ExecutionListener` or `TaskListener`, and it is not declared in the
BPMN model.

The adapter delivery passes its whole per-task work to the decorators: the call of the handler and the
delivery's own failure handling and logging. Code placed before and after `execution.run()` therefore
runs before and after both. A span or MDC entry opened around `execution.run()` also covers the
handler's logs, the logs of the `process-engine-worker`, and the logs the delivery writes when it
reports a failure.

A decorator cannot change the task:

- It receives read-only views of the task information and the payload. Do not mutate values inside
  the payload, as they are shared with the handler.
- Call `execution.run()` exactly once, in the thread of the decorated runnable. The adapter runs the
  execution exactly once anyway: if a decorator skips it, throws before running it, or runs it twice,
  this is only logged.
- An exception a decorator throws is only logged, except a `VirtualMachineError`. An exception thrown
  by the execution itself passes through the decorators unchanged.
- The delivery handles a failing handler itself, so `execution.run()` returns normally even if the
  task failed. A decorator does not learn the outcome; the failure is visible in the logs written
  inside its scope.

Several decorators can be registered. They are nested in execution order, the first one outermost,
so put tracing first to give the inner decorators' logs the trace id. Decorators run synchronously in
the delivery thread. Keep them cheap and non-blocking, and release what you open in a `finally` block,
otherwise a scope or MDC entry leaks into the next task on this thread.

Decorators only take effect with an adapter that supports them. The following example assumes an
adapter starter that collects them as Spring beans and orders them by `@Order`/`Ordered`. It runs
every task in its own span and puts the activity id into the MDC.

```kotlin
@Component
@Order(0)
class TracingTaskExecutionDecorator(private val tracer: Tracer) : TaskExecutionDecorator {

  override fun decorate(task: TaskInformation, payload: Map<String, Any?>, execution: Runnable) = Runnable {
    val span = tracer.spanBuilder("${task.meta["topicName"]}.handle")
      .setNoParent()
      .setSpanKind(SpanKind.CONSUMER)
      .setAttribute("engine.task.id", task.taskId)
      .startSpan()
    try {
      span.makeCurrent().use {
        MDC.putCloseable("activityId", task.meta[CommonRestrictions.ACTIVITY_ID]).use {
          execution.run()
        }
      }
    } finally {
      span.end()
    }
  }
}
```

The same in Java, as a lambda that logs before and after each task:

```java
@Bean
@Order(1)
TaskExecutionDecorator loggingTaskExecutionDecorator() {
  return (task, payload, execution) -> () -> {
    log.info("Processing task {} with variables {}", task.getTaskId(), payload);
    try {
      execution.run();
    } finally {
      log.info("Finished task {}", task.getTaskId());
    }
  };
}
```

### Supporting decorators in an adapter

An adapter creates one `TaskExecutionDecoration` (module `process-engine-api-impl`) from the
decorators in execution order, passes it to its deliveries, and calls `run` in each delivery of
external tasks, around the whole per-task work including its failure handling.
`TaskExecutionDecoration.NONE` just runs the work.

```kotlin
decoration.run(taskInformation, payload) {
  try {
    subscription.action.accept(taskInformation, payload)
  } catch (e: NotFoundException) {
    logger.debug(e) { "Task ${taskInformation.taskId} is gone." }
  } catch (e: Exception) {
    logger.warn(e) { "Failed delivering task ${taskInformation.taskId}." }
    externalTaskService.handleFailure(taskInformation.taskId, workerId, e.message, retries - 1, retryTimeout)
  }
}
```
