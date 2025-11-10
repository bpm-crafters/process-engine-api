---
title: Service Task Completion API
---

The Service Task API provides functionality to deal with service tasks. The task handlers can be registered
and get invoked when tasks appear in the process engine. Since the Task Subscription API allows asynchronous processing,
we provide a special API to complete tasks.

The FailTaskCmd also allows to pass the number of remaining retries and the backoff timeout for the next retry.

```java

@Component
@RequiredArgsConstructor
public class PerformServiceTaskUseCase {

  private final ServiceTaskCompletionApi taskCompletionApi;
  
  public void complete(String taskId, String value) {
    taskCompletionApi.completeTask(
      new CompleteTaskCmd(
        taskId,
        () -> Map.of("some-user-value", value)
      )
    ).get();
  }
  
  public void completeWithError(String taskId, String value) {
    taskCompletionApi.completeTaskByError(
      new CompleteTaskByErrorCmd(
        taskId,
        "user_error",
        "some error",
        () -> Map.of("some-user-value", value)
      )
    ).get();
  }
  
  public void fail(String taskId, Exception e) {
    taskCompletionApi.failTask(new FailTaskCmd(
        taskId,
        e.getMessage(),
        null
      )
    ).get();
  }

  public void failWithRetry(String taskId, int retries, Duration retryBackoff, Exception e) {
    taskCompletionApi.failTask(new FailTaskCmd(
        taskId,
        e.getMessage(),
        null,
        retryCount,
        retryBackoff
      )
    ).get();
  }
  
}

```
