package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.ServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder
import org.camunda.bpm.engine.externaltask.LockedExternalTask

/**
 * Delivers external tasks to subscriptions.
 * This implementation uses internal Java API and pulls tasks for delivery.
 */
class RemotePullServiceTaskDelivery(
  private val externalTaskService: ExternalTaskService,
  private val workerId: String,
  private val subscriptionRepository: SubscriptionRepository,
  private val maxTasks: Int,
  private val lockDuration: Long,
  private val retryTimeout: Long
) : ServiceTaskDelivery, RefreshableDelivery {

  companion object : KLogging()

  /**
   * Delivers all tasks found in the external service to corresponding subscriptions.
   */
  override fun refresh() {

    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if(subscriptions.isNotEmpty()) {
      logger.trace { "Pull remote external tasks for subscriptions: $subscriptions" }
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

              val variables = if (activeSubscription.payloadDescription == null) {
                lockedTask.variables
              } else {
                if (activeSubscription.payloadDescription!!.isEmpty()) {
                  mapOf()
                } else {
                  lockedTask.variables.filter { activeSubscription.payloadDescription!!.contains(it.key) }
                }
              }
              try {
                activeSubscription.action.accept(lockedTask.toTaskInformation(), variables)
              } catch (e: Exception) {
                externalTaskService.handleFailure(lockedTask.id, workerId, e.message, lockedTask.retries - 1, retryTimeout)
              }
            }
        }
    } else {
      logger.trace { "Pull remote external tasks disabled because of no active subscriptions" }
    }
  }

  private fun ExternalTaskQueryBuilder.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): ExternalTaskQueryBuilder {
    subscriptions
      .mapNotNull { it.taskDescriptionKey }
      .distinct()
      .forEach { topic ->
        this.topic(topic, lockDuration)
          .enableCustomObjectDeserialization()
        // FIXME ->
      }
    return this
  }

  private fun TaskSubscriptionHandle.matches(task: LockedExternalTask): Boolean {
    return this.taskType == TaskType.EXTERNAL
      && (this.taskDescriptionKey == null || this.taskDescriptionKey == task.topicName)
    // FIXME -> more descriptions
  }
}
