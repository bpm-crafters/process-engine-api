package dev.bpmcrafters.processengineapi.task

/**
 * Command for unsubscribing from task.
 * @since 0.0.1
 */
data class UnsubscribeFromTaskCmd (
  /**
   * Task subscription.
   */
  val subscription: TaskSubscription
)
