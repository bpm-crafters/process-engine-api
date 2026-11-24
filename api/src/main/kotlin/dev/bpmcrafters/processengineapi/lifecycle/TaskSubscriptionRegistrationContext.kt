package dev.bpmcrafters.processengineapi.lifecycle

import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi

/**
 * Lifecycle context for the synchronous task-subscription registration window.
 *
 * @since 1.8.0
 */
class TaskSubscriptionRegistrationContext(
  private val taskSubscriptionApi: TaskSubscriptionApi
) : ProcessApplicationLifecycleContext(ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION) {

  /**
   * Returns the API used to register task subscriptions during this phase.
   */
  fun getTaskSubscriptionApi(): TaskSubscriptionApi = taskSubscriptionApi
}
