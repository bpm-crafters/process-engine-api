package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.ExternalServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
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
class EmbeddedPullServiceTaskDelivery(
  private val externalTaskService: ExternalTaskService,
  private val workerId: String,
  private val subscriptionRepository: SubscriptionRepository,
  private val maxTasks: Int,
  private val lockDuration: Long,
  private val retryTimeout: Long
) : ExternalServiceTaskDelivery, RefreshableDelivery {

  companion object : KLogging()

  /**
   * Delivers all tasks found in the external service to corresponding subscriptions.
   */
  override fun refresh() {

    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if(subscriptions.isNotEmpty()) {
      logger.trace { "Pull external tasks for subscriptions: $subscriptions" }
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
                logger.error { "[PROCESS-ENGINE-C7-EMBEDDED]: Error delivering task ${lockedTask.id}: ${e.message}" }
                subscriptionRepository.deactivateSubscriptionForTask(taskId = lockedTask.id)
              }
            }
        }
    } else {
      logger.trace { "Pull external tasks disabled because of no active subscriptions" }
    }
  }

  private fun ExternalTaskQueryBuilder.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): ExternalTaskQueryBuilder {
    subscriptions
      .mapNotNull { it.taskDescriptionKey }
      .distinct()
      .forEach { topic ->
        this.topic(topic, lockDuration)
          // .enableCustomObjectDeserialization()
      }
    return this
  }

  private fun TaskSubscriptionHandle.matches(task: LockedExternalTask): Boolean {
    return this.taskType == TaskType.EXTERNAL
      && (this.taskDescriptionKey == null || this.taskDescriptionKey == task.topicName)
      && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == task.executionId
        CommonRestrictions.ACTIVITY_ID -> it.value == task.activityId
        CommonRestrictions.BUSINESS_KEY -> it.value == task.businessKey
        CommonRestrictions.TENANT_ID -> it.value == task.tenantId
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == task.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_KEY -> it.value == task.processDefinitionKey
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == task.processDefinitionId
        CommonRestrictions.PROCESS_DEFINITION_VERSION_TAG -> it.value == task.processDefinitionVersionTag
        else -> false
      }
    }
  }
}
