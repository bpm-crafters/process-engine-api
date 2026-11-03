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

## Task handler observers

Cross-cutting concerns such as logging, tracing, or auditing can observe every task delivery via a
`TaskHandlerObserver`. An observer applies to both registration paths (`@ProcessEngineWorker` and
manual `subscribeForTask`) and to every `TaskType`. Filter on `context.getTaskType()` if an observer
should only act on a specific type.

An observer watches the handler call of a subscription in the application. It is not an engine
listener such as a Camunda `ExecutionListener` or `TaskListener`, and it is not declared in the BPMN
model.

The handler always runs exactly once, and `before` is called before it in list order, `after` after
it in reverse order. An observer cannot affect the task:

- Everything thrown by `before` or `after` is only logged, except a `VirtualMachineError` such as
  `OutOfMemoryError`. If `before` throws, `after` is not called for this observer; the handler and
  all other observers still run.
- An exception thrown by the handler reaches `after` as `error` and is then passed on to the
  delivery unchanged.
- The context is read-only. The handler receives the original arguments, so changing the context
  never reaches it. The payload is a view, not a copy: do not mutate values inside it, as they are
  shared with the handler, and expect changes the handler makes to a mutable payload map to be
  visible in `after`.

`before` returns a state that the same observer receives in `after`, so no `ThreadLocal` is needed
and no observer can see the state of another. If no state is needed, use `Unit` in Kotlin, or `Void`
in Java and return `null` from `before`.

Observers run synchronously in the delivery thread. Work in `before` delays the handler and uses up
the lock time of the task, and work in `after` blocks the worker thread. Keep them cheap and
non-blocking, and do not call external systems synchronously. Acquire resources in `before` all or
nothing, and release them in `after` in a `finally` block: a scope or MDC entry left open leaks into
the next task on this thread.

A handler may report a failure or a BPMN error itself instead of throwing an exception, as the
`process-engine-worker` does. The `TaskHandlerOutcome` passed to `after` tells what happened:
`Completed`, `CompletedByError`, `Failed`, or `Undetermined`. From Java, the singletons are
`TaskHandlerOutcome.Completed.INSTANCE` and `TaskHandlerOutcome.Undetermined.INSTANCE`; match the
others with `instanceof`. The outcome is only known if the handler finishes the task in the delivery
thread through a `ServiceTaskCompletionApi` wrapped in `ObserverAwareServiceTaskCompletionApi`
(module `process-engine-api-impl`). The adapter has to wrap its completion API and pass the
observers; either one alone makes every outcome `Undetermined`. A task finished later, by another
thread, or not through a `ServiceTaskCompletionApi` (as user tasks are) results in `Undetermined`.
`Undetermined` means neither success nor failure, so do not treat it as either. The outcome reflects
the accepted request, not the result of the returned future. A completion call that throws
synchronously results in `Undetermined`.

An adapter passes observers to the constructor of `AbstractTaskSubscriptionApiImpl`, in execution
order. Observers therefore only take effect with an adapter that supports them. The following
example assumes an adapter starter that collects them as Spring beans and orders them by
`@Order`/`Ordered`. It runs every task in its own span and puts the activity id into the MDC.

```java
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.TaskHandlerContext;
import dev.bpmcrafters.processengineapi.task.TaskHandlerObserver;
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class TracingTaskHandlerObserver implements TaskHandlerObserver<TracingTaskHandlerObserver.OpenSpan> {

  public record OpenSpan(Span span, Scope scope, MDC.MDCCloseable mdc) {}

  private final Tracer tracer;

  @Override
  public OpenSpan before(TaskHandlerContext context) {
    String activityId = context.getTaskInformation().getMeta().getOrDefault(CommonRestrictions.ACTIVITY_ID, "");
    Span span = tracer.spanBuilder(context.getTaskDescriptionKey() + ".handle")
      .setNoParent()
      .setSpanKind(SpanKind.CONSUMER)
      .startSpan();
    Scope scope = span.makeCurrent();
    MDC.MDCCloseable mdc = MDC.putCloseable("activityId", activityId);
    return new OpenSpan(span, scope, mdc);
  }

  @Override
  public void after(TaskHandlerContext context, OpenSpan state, TaskHandlerOutcome outcome, Throwable error) {
    try {
      if (error != null) {
        state.span().recordException(error).setStatus(StatusCode.ERROR);
      } else if (outcome instanceof TaskHandlerOutcome.Failed failed) {
        state.span().setStatus(StatusCode.ERROR, failed.getReason());
      }
    } finally {
      state.mdc().close();
      state.scope().close();
      state.span().end();
    }
  }
}
```

An observer without state, for example for logging:

```java
@Component
@Slf4j
public class LoggingTaskHandlerObserver implements TaskHandlerObserver<Void> {

  @Override
  public Void before(TaskHandlerContext context) {
    return null;
  }

  @Override
  public void after(TaskHandlerContext context, Void state, TaskHandlerOutcome outcome, Throwable error) {
    log.info("[TASK]: {} finished with {}", context.getTaskInformation().getTaskId(), outcome);
  }
}
```
