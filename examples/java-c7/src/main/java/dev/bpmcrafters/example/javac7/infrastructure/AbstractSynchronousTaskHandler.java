package dev.bpmcrafters.example.javac7.infrastructure;

import dev.bpmcrafters.processengineapi.task.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;


@Slf4j
public abstract class AbstractSynchronousTaskHandler {
  private final TaskApi taskApi;
  private final String topic;
  private TaskSubscription subscription;

  public AbstractSynchronousTaskHandler(TaskApi taskApi, String topic) {
    this.taskApi = taskApi;
    this.topic = topic;
  }

  @SneakyThrows
  public void register() {
    log.info("Registering handler for {}", topic);
    this.subscription = this.taskApi.subscribeForTask(
      new SubscribeForTaskCmd(Collections.emptyMap(), topic, Collections.emptySet(), (taskId, variables) -> {
        try {
          log.info("Completing task {}...", taskId);
          taskApi.completeTask(new CompleteTaskCmd(taskId, () -> execute(taskId, variables)));
          log.info("Completed task {}.", taskId);
        } catch (TaskHandlerException e) {
          log.info("Error completing task {}, completing with error code {}.", taskId, e.getErrorCode());
          taskApi.completeTaskByError(new CompleteTaskByErrorCmd(taskId, e.getErrorCode()));
        }
      })
    ).get();
  }

  @SneakyThrows
  public void unregister() {
    log.info("Un-registering handler for {}", topic);
    this.taskApi.unsubscribe(new UnsubscribeFromTaskCmd(subscription)).get();
  }

  public abstract Map<String, Object> execute(String taskId, Map<String, ?> variables) throws TaskHandlerException;

}
