package dev.bpmcrafters.example.javac7.infrastructure;

import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;


/**
 * Abstract example implementation of synchronous task handler.
 */
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
      new SubscribeForTaskCmd(
        CommonRestrictions.builder().withTaskType("service").build(),
        topic,
        Collections.emptySet(),
        (taskInfo, variables) -> {
          try {
            log.info("[SYNC HANDLER]: Executing task {}...", taskInfo.getTaskId());
            taskApi.completeTask(new CompleteTaskCmd(taskInfo.getTaskId(), () -> execute(taskInfo, variables)));
            log.info("[SYNC HANDLER]: Completed task {}.", taskInfo.getTaskId());
          } catch (TaskHandlerException e) {
            log.info("[SYNC HANDLER]: Error completing task {}, completing with error code {}.", taskInfo.getTaskId(), e.getErrorCode());
            taskApi.completeTaskByError(new CompleteTaskByErrorCmd(taskInfo.getTaskId(), e.getErrorCode()));
          }
        },
        TaskModificationHandler.getEmpty()
      )
    ).get();
  }

  @SneakyThrows
  public void unregister() {
    log.info("Un-registering handler for {}", topic);
    this.taskApi.unsubscribe(new UnsubscribeFromTaskCmd(subscription)).get();
  }

  public abstract Map<String, Object> execute(TaskInformation taskInfo, Map<String, ?> variables) throws TaskHandlerException;

}
