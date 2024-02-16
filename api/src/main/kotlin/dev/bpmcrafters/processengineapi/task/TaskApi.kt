package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.RestrictionAware
import java.util.concurrent.Future

/**
 * Task API providing operations for task handling and for subscription of the task handlers.
 * @since 0.0.1
 */
interface TaskApi : MetaInfoAware, RestrictionAware {

  /**
   * Creates a subscription for the task which will deliver tasks matching the [SubscribeForTaskCmd.taskDescriptionKey]
   * and [SubscribeForTaskCmd.restrictions] to the provided [SubscribeForTaskCmd.action]. The action will receive
   * the entire payload available to the task, or if [SubscribeForTaskCmd.payloadDescription] is specified, the
   * set of requested variables only.
   * @param cmd command to subscribe.
   * @return future indicating the completion, containing the task subscription.
   */
  fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription>

  /**
   * Cancels the subscription.
   *
   * @param cmd command to unsubscribe.
   * @return future indicating the completion.
   */
  fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Empty>

  /**
   * Complete the task.
   * @param cmd command to complete the task.
   * @return future indicating the completion.
   */
  fun completeTask(cmd: CompleteTaskCmd): Future<Empty>

  /**
   * Completes the task by throwing an error.
   * @param cmd command to complete the task.
   * @return future indicating the completion.
   */
  fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty>
}
