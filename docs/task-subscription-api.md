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

## Intercepting task handlers

Cross-cutting concerns such as logging, tracing, or auditing can wrap every task delivery via a
`TaskHandlerInterceptor`. An interceptor wraps the call of the subscription action for both
registration paths (`@ProcessEngineWorker` and manual `subscribeForTask`) and applies to every
`TaskType`. Filter on `context.getTaskType()` if an interceptor should only act on a specific type.

Interceptors run in the delivery thread, inside the delivery's try/catch. An exception thrown by an
interceptor before the handler returned is treated like a handler exception and leads to failure
handling or a retry by the adapter. An exception thrown after the handler returned is deliberately
only logged, because the task may already be finished. Enclosing interceptors still see such an
exception from `chain.proceed()`. An interceptor that swallows a handler exception makes the delivery
see a success. The context is read-only; interceptors observe the delivery but do not mutate payload
or task information. Calling `chain.proceed()` again invokes the handler again.

An exception thrown before the handler ran fails the delivery and consumes a retry, although the
handler was not executed. Throw only to veto the execution deliberately. Secure fallible work such
as a synchronous audit call inside the interceptor.

A handler may report a failure or a BPMN error itself instead of throwing an exception, as the
`process-engine-worker` does. `chain.proceed()` then returns normally, and the returned
`TaskHandlerOutcome` tells what happened: `Completed`, `CompletedByError`, `Failed`, or
`Undetermined`. The outcome is only known if the handler finishes the task in the delivery thread
through a `ServiceTaskCompletionApi` wrapped in `InterceptorAwareServiceTaskCompletionApi`. A task
finished later or by another thread results in `Undetermined`, as does every `USER` task.
`Undetermined` means neither success nor failure, so do not treat it as either. The
outcome reflects the accepted request, not the result of the returned future. A completion call
that throws synchronously reports nothing.

An adapter passes interceptors to the constructor of `AbstractTaskSubscriptionApiImpl`, in execution
order. Interceptors therefore only take effect with an adapter that supports them. The following
example assumes an adapter starter that collects them as Spring beans and orders them by
`@Order`/`Ordered`.

```java

@Component
@Slf4j
@Order(0)
public class LoggingTaskHandlerInterceptor implements TaskHandlerInterceptor {

  @Override
  public void intercept(TaskHandlerInterceptorContext context, TaskHandlerInterceptorChain chain) {
    log.info("[TASK]: Handling {} of type {}", context.getTaskInformation().getTaskId(), context.getTaskType());
    try {
      TaskHandlerOutcome outcome = chain.proceed();
      if (outcome instanceof TaskHandlerOutcome.Failed failed) {
        log.error("[TASK]: Failed handling {}: {}", context.getTaskInformation().getTaskId(), failed.getReason());
      } else if (outcome instanceof TaskHandlerOutcome.Undetermined) {
        log.info("[TASK]: Handler returned for {}, outcome unknown", context.getTaskInformation().getTaskId());
      } else {
        log.info("[TASK]: Handled {}", context.getTaskInformation().getTaskId());
      }
    } catch (RuntimeException e) {
      log.error("[TASK]: Failed handling {}", context.getTaskInformation().getTaskId(), e);
      throw e;
    }
  }
}


```
