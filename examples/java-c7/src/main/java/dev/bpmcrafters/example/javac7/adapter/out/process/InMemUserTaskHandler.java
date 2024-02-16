package dev.bpmcrafters.example.javac7.adapter.out.process;

import dev.bpmcrafters.example.javac7.application.port.out.UserTaskOutPort;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class InMemUserTaskHandler implements UserTaskOutPort {

  private final TaskApi taskApi;
  private final Map<TaskInformation, Map<String, ?>> userTasks = new ConcurrentHashMap<>();

  @SneakyThrows
  public void register() {
    taskApi.subscribeForTask(
      new SubscribeForTaskCmd(
        CommonRestrictions.builder().withTaskType("user").build(), // user tasks only
        null, // all of them
        Collections.emptySet(), // all variables
        (taskInfo, variables) -> {
          if (!userTasks.containsKey(taskInfo)) { // don't replace existing tasks
            log.info("Received user task {} with meta {}", taskInfo.getTaskId(), taskInfo.getMeta());
            userTasks.put(taskInfo, variables);
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
