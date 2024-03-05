package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.ExternalTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder
import org.camunda.bpm.engine.externaltask.LockedExternalTask

/**
 * Delivers external tasks to subscriptions.
 * This implementation uses internal Java API and pulls tasks for delivery.
 */
class EmbeddedPullExternalTaskDelivery(
  private val externalTaskService: ExternalTaskService,
  private val workerId: String,
  private val subscriptionRepository: SubscriptionRepository,
  private val maxTasks: Int,
  private val lockDuration: Long
) : ExternalServiceTaskDelivery {

  /**
   * Delivers all tasks found in the external service to corresponding subscriptions.
   */
  fun deliverAll() {

    val subscriptions = subscriptionRepository.getTaskSubscriptions()

    // FIXME -> how many queries do we want? 1:1 subscriptions, or 1 query for all?
    externalTaskService
      .fetchAndLock(maxTasks, workerId)
      .forSubscriptions(subscriptions)
      .execute()
      .forEach { lockedTask ->
        subscriptions
          .firstOrNull { subscription -> subscription.matches(lockedTask) }
          ?.let { activeSubscription ->

            subscriptionRepository.activateSubscriptionForTask(lockedTask.id, activeSubscription)

            val variables = if (activeSubscription.payloadDescription.isEmpty()) {
              lockedTask.variables
            } else {
              lockedTask.variables.filter { activeSubscription.payloadDescription.contains(it.key) }
            }
            try {
              activeSubscription.action.accept(lockedTask.toTaskInformation(), variables)
            } catch (e: Exception) {
              externalTaskService.handleFailure(lockedTask.id, workerId, e.message, lockedTask.retries - 1, 10) // FIXME -> props
            }
          }
      }
  }

  private fun ExternalTaskQueryBuilder.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): ExternalTaskQueryBuilder {
    subscriptions
      .mapNotNull { it.taskDescriptionKey }
      .distinct()
      .forEach { topic ->
        this.topic(topic, lockDuration)
        // FIXME ->
      }
    return this
  }

  private fun TaskSubscriptionHandle.matches(task: LockedExternalTask): Boolean {
    return ExternalTaskCompletionStrategy.supports(this.restrictions) &&
      (this.taskDescriptionKey == null || this.taskDescriptionKey == task.topicName)
    // FIXME -> more descriptions
  }
}
