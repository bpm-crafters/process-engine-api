package dev.bpmcrafters.example.common.adapter.out.process;

import dev.bpmcrafters.example.common.application.port.out.UserTaskOutPort;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class InMemUserTaskAdapter implements UserTaskOutPort {

  private final TaskApi taskApi;
  private final Map<TaskInformation, Map<String, ?>> userTasks = new ConcurrentHashMap<>();

  @SneakyThrows
  public void register() {
    taskApi.subscribeForTask(
      new SubscribeForTaskCmd(
        CommonRestrictions.builder().userTasks().build(), // user tasks only
        null, // all of them
        Collections.emptySet(), // all variables
        (taskInfo, variables) -> {
          if (!userTasks.containsKey(taskInfo)) { // don't replace existing tasks
            log.info("[TASK LIST]: Received user task {} with meta {}", taskInfo.getTaskId(), taskInfo.getMeta());
            userTasks.put(taskInfo, variables);
          }
        },
        new TaskTerminationHandler() {
          @Override
          public void terminated(@NotNull String taskId) {
            log.info("[TASK LIST]: Removing task {}", taskId);
            userTasks.keySet().stream().filter(info -> info.getTaskId().equals(taskId)).findFirst().ifPresent(userTasks::remove);
          }
        }
      )
    ).get();
  }

  @Override
  public List<TaskInformation> getAllTasks() {
    return userTasks.keySet().stream().toList();
  }

  @Override
  public Optional<Map<String, ?>> getTaskVariablesById(String taskId) {
    return userTasks.entrySet().stream().filter((taskInfo) -> taskInfo.getKey().getTaskId().equals(taskId)).findFirst().map(Map.Entry::getValue);
  }

}
