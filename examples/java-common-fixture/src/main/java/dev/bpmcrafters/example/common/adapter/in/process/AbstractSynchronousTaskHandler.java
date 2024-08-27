package dev.bpmcrafters.example.common.adapter.in.process;

import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 * Abstract example implementation of synchronous task handler.
 */
@Slf4j
public abstract class AbstractSynchronousTaskHandler {
  private final TaskSubscriptionApi taskSubscriptionApi;
  private final ServiceTaskCompletionApi externalTaskCompletionApi;
  private final String topic;
  private TaskSubscription subscription;

  public AbstractSynchronousTaskHandler(TaskSubscriptionApi taskSubscriptionApi, ServiceTaskCompletionApi externalTaskCompletionApi, String topic) {
    this.taskSubscriptionApi = taskSubscriptionApi;
    this.externalTaskCompletionApi = externalTaskCompletionApi;
    this.topic = topic;
  }

  @SneakyThrows
  public void register() {
    log.info("[EXTERNAL TASK HANDLER] Registering handler for {}", topic);
    this.subscription = this.taskSubscriptionApi.subscribeForTask(
      new SubscribeForTaskCmd(
        CommonRestrictions.builder().build(),
        TaskType.EXTERNAL,
        topic,
        null,
        (taskInfo, variables) -> {
          try {
            log.info("[SYNC HANDLER]: Executing task {}...", taskInfo.getTaskId());
            externalTaskCompletionApi.completeTask(new CompleteTaskCmd(taskInfo.getTaskId(), () -> execute(taskInfo, variables)));
            log.info("[SYNC HANDLER]: Completed task {}.", taskInfo.getTaskId());
          } catch (TaskHandlerException e) {
            log.info("[SYNC HANDLER]: Error completing task {}, completing with error code {}.", taskInfo.getTaskId(), e.getErrorCode());
            externalTaskCompletionApi.completeTaskByError(new CompleteTaskByErrorCmd(
                taskInfo.getTaskId(),
                e.getErrorCode(),
                e.getMessage(),
                e::getPayload
              )
            );
          } catch (Exception e) {
            log.info("[SYNC HANDLER]: Failed handling task {}, completing with exception {}.", taskInfo.getTaskId(), e.getMessage());
            externalTaskCompletionApi.failTask(new FailTaskCmd(
                taskInfo.getTaskId(),
                e.getMessage(),
                null
              )
            );
          }
        },
        (taskId) -> {} // nothing to do
      )
    ).get();
  }

  @SneakyThrows
  public void unregister() {
    log.info("[EXTERNAL TASK HANDLER] Un-registering handler for {}", topic);
    this.taskSubscriptionApi.unsubscribe(new UnsubscribeFromTaskCmd(subscription)).get();
  }

  public abstract Map<String, Object> execute(TaskInformation taskInfo, Map<String, ?> variables) throws TaskHandlerException;

}
