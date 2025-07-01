---
title: User Task Completion API
---

The User Task Completion API provides functionality to modify existing user tasks. Since the Task Subscription API allows asynchronous processing,
we provide a special API to modify existing tasks. 

```java

@Component
@RequiredArgsConstructor
public class ModifyUserTaskUseCase {

  private final UserTaskMdificationApi taskModificationApi;
  
  public void modify(String taskId, String value) {
    taskModificationApi.update(
      new AssignTaskCmd(
        taskId,
        value
      )
    ).get();
  }
}

```

The following modifications are available:

## Changing payload

| Intent                     | Command                |
|----------------------------|------------------------|
| Update payload             | `UpdatePayloadTaskCmd` |
| Clear payload              | `ClearPayloadTaskCmd`  |
| Delete part of the payload | `DeletePayloadTaskCmd` |

## Changing assignment


| Intent                 | Command                       |
|------------------------|-------------------------------|
| Set assignee           | `AssignTaskCmd`               |
| Clear assignee         | `UnassignTaskCmd`             |
| Add candidate group    | `AddCandidateGroupTaskCmd`    |
| Remove candidate group | `RemoveCandidateGroupTaskCmd` |
| Set candidate groups   | `SetCandidateGroupTaskCmd`    |
| Clear candidate groups | `ClearCandidateGroupTaskCmd`  |
| Add candidate user     | `AddCandidateUserTaskCmd`     |
| Remove candidate user  | `RemoveCandidateUserTaskCmd`  |
| Set candidate user     | `SetCandidateUserTaskCmd`     |
| Clear candidate user   | `ClearCandidateUserTaskCmd`   |

## Composing multiple changes on one request

If you want to create multiple modifications of the task at the same time, you can use `CompositeModifyTaskCmd`. Please note
that multiple modifications on the same task are supported only. To create the command easily, we supplied a 
fluent builder:

```java 

@Component
@RequiredArgsConstructor
public class ModifyUserTaskUseCase {

  private final UserTaskMdificationApi taskModificationApi;

  public void modify(String taskId, String value) {
    taskModificationApi.update(
      TaskModification("taskId")
        .assign("kermit")
        .unassign()
        .clearCandidateUsers()
        .clearCandidateGroups()
        .addCandidateGroup("avengers")
        .removeCandidateGroup("muppets")
        .addCandidateUser("kermit")
        .removeCandidateUser("piggy")
        .setCandidateUsers(listOf("fozzy"))
        .setCandidateGroups(listOf("avengers"))
        .updatePayload(Maps.of("task-modified", true))
        .deletePayload(List.of("task-modified"))
        .clearPayload()
        .build()
    ).get();
  }
}
```

If you are using Kotlin you can use receiver functions instead:

```kotlin
TaskModification.taskModification("taskId") {
  unassign()
  assign("kermit")
  setCandidateUsers(listOf("piggy"))
  addCandidateUser("kermit")
  removeCandidateUser("piggy")
  setCandidateGroups(listOf("avengers"))
  addCandidateGroup("muppets")
  removeCandidateGroup("avengers")
  clearCandidateGroups()
  clearCandidateUsers()
  updatePayload {
    mapOf("task-modified" to true)
  }
  clearPayload()
  deletePayload {
    listOf("some-var-other")
  }
}
```

