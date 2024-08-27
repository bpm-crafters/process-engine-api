---
title: User Task Completion API
---

The User Task Completion API provides functionality to deal with user tasks. Since the Task Subscription API allows asynchronous processing,
we provide a special API to complete tasks. 

```java

@Component
@RequiredArgsConstructor
public class PerformUserTaskUseCase {

  private final UserTaskCompletionApi taskCompletionApi;
  
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
}

```
