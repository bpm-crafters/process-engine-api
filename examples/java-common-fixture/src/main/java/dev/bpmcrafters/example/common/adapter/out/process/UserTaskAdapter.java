package dev.bpmcrafters.example.common.adapter.out.process;

import dev.bpmcrafters.example.common.application.port.out.UserTaskOutPort;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class UserTaskAdapter implements UserTaskOutPort {

  private final UserTaskSupport userTaskSupport;
  private final UserTaskCompletionApi taskCompletionApi;

  @Override
  public List<TaskInformation> getAllTasks() {
    return userTaskSupport.getAllTasks();
  }

  @Override
  public Optional<Map<String, ?>> getTaskVariablesById(String taskId) {
    try {
      return Optional.of(userTaskSupport.getPayload(taskId));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  @SneakyThrows
  public void complete(String taskId, String value) {
    taskCompletionApi.completeTask(
      new CompleteTaskCmd(
        taskId,
        () -> Map.of("some-user-value", value)
      )
    ).get();
  }

  @Override
  @SneakyThrows
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
