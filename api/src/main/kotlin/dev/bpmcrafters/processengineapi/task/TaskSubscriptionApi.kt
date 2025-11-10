package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.RestrictionAware
import java.util.concurrent.CompletableFuture

/**
 * Task API providing operations for subscription of the task handlers.
 * @since 0.0.1
 */
interface TaskSubscriptionApi : MetaInfoAware, RestrictionAware {

  /**
   * Creates a subscription for the task which will deliver tasks matching the [SubscribeForTaskCmd.taskDescriptionKey]
   * and [SubscribeForTaskCmd.restrictions] to the provided [SubscribeForTaskCmd.action]. The action will receive
   * the entire payload available to the task, or if [SubscribeForTaskCmd.payloadDescription] is specified, the
   * set of requested variables only.
   * @param cmd command to subscribe.
   * @return future indicating the completion, containing the task subscription.
   */
  fun subscribeForTask(cmd: SubscribeForTaskCmd): CompletableFuture<TaskSubscription>

  /**
   * Cancels the subscription.
   *
   * @param cmd command to unsubscribe.
   * @return future indicating the completion.
   */
  fun unsubscribe(cmd: UnsubscribeFromTaskCmd): CompletableFuture<Empty>
}
