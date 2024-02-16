package dev.bpmcrafters.example.javac7.adapter.out;

import dev.bpmcrafters.example.javac7.application.port.UserTaskOutPort;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class InMemUserTaskHandler implements UserTaskOutPort {

  private final TaskApi taskApi;
  private final Map<String, Map<String, ?>> userTasks = new ConcurrentHashMap<>();

  @SneakyThrows
  public void register() {
    taskApi.subscribeForTask(
      new SubscribeForTaskCmd(
        CommonRestrictions.builder().withTaskType("user").build(), // user tasks only
        null, // all of them
        Collections.emptySet(), // all variables
        userTasks::put // just add to the map
      )
    ).get();
  }

  @Override
  public Map<String, Map<String, ?>> getAllTasks() {
    return userTasks;
  }
}
