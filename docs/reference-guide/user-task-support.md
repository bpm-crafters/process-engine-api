---
title: User Task Support
---

## Configuration

The [User Task Support](user-task-support.md) is a small component simplifying use cases related to user tasks. It can be hooked up into Task Subscription API
and receive and store the received user tasks. If later you want to access a task delivered in the past, its `TaskInformation` and payload are available in the
User Task Support component.

User Task Support is a passive component and needs to be instantiated and registered to the subscription API. This behaviour is implemented in order to allow 
the user of it, to customize the subscription restrictions (e.g. subscribing only for a certain process definition key, or certain tenant). 

To configure and register `User Task Support` use the following code:

```java

import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;

@Configuration
@RequiredArgsConstructor
public class UserTaskSupportConfiguration {
  
  @Bean
  public UserTaskSupport createAndRegisterUserTaskSupport(TaskSubscriptionApi taskSubscriptionApi) {
    UserTaskSupport support = new UserTaskSupport();
    support.subscribe(
      taskSubscriptionApi, 
      CommonRestrictions.builder().build(), 
      null, // String parameter to limit the taskDescriptionKey, see SubscribeForTaskCmd 
      null // no restriction of the payload, see SubscribeForTaskCmd
    );
    return support;
  }
  
}
```

User Task Support provides API for the following User-Task-related use cases. For retrieving information and payload about the task by given 
task id (task existence is enforced) or information of all tasks:

  - `getTaskInformation(taskId: String): TaskInformation` 
  - `getPayload(taskId: String): Map<String, Any>`
  - `getAllTasks(): List<TaskInformation>`

Checking or enforce task existence:
  
  - `exists(taskId: String, activityId: String?): Boolean`
  - `requireTask(taskId: String, activityId: String?)`

In addition to the API for task retrieval offered by the `User Task Support` component, it allows to register additional `TaskHandler` and `TaskTerminationHandler`
directly and acts as a composite handler, invoking both on corresponding task lifecycle events. This is in particular helpful, if you implement any kind
of forwarding or notification logic for the user tasks:

  - `addTaskHandler(handler: TaskHandler)`
  - `addTaskTerminationHandler(handler: TaskTerminationHandler)`

## Typical usage

Typically, implementation of task-related use cases require access via inbound adapters to your use case based on some reference to the user task. Often, systems for 
delivery of user tasks or user tasks notification will use the task id as the only reference to the task. The use case implementation needs to resolve the reference, 
load some information, which is shown to user on a user task form and finally receive a task command executing one of the common task operations (complete, etc...)

For the implementation of those, you would need to outbound adapters: `UserTaskSupport` for resolving task reference and `UserTaskCompletionAPI` for sending out commands.
See our provided examples for more details.

