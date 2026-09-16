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
interceptor is treated like a handler exception and leads to failure handling or a retry by the
adapter. The context is immutable; interceptors observe the delivery but do not mutate payload or
task information.

Register interceptors as Spring beans in the adapter starter. The order follows `@Order`/`Ordered`.

```java

@Component
@Slf4j
@Order(0)
public class LoggingTaskHandlerInterceptor implements TaskHandlerInterceptor {

  @Override
  public void intercept(TaskHandlerInterceptorContext context, TaskHandlerInterceptorChain chain) {
    log.info("[TASK]: Handling {} of type {}", context.getTaskInformation().getTaskId(), context.getTaskType());
    try {
      chain.proceed();
      log.info("[TASK]: Handled {}", context.getTaskInformation().getTaskId());
    } catch (RuntimeException e) {
      log.error("[TASK]: Failed handling {}", context.getTaskInformation().getTaskId(), e);
      throw e;
    }
  }
}


```
