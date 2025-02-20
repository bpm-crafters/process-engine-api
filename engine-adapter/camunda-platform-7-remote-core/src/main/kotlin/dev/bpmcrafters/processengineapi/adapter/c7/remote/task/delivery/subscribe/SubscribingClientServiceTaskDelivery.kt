package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.subscribe

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder

private val logger = KotlinLogging.logger {}

/**
 *
 */
class SubscribingClientServiceTaskDelivery(
  private val externalTaskClient: ExternalTaskClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val lockDuration: Long,
  private val retryTimeout: Long,
  private val retries: Int,
  ) {

  fun subscribe() {

    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C7-REMOTE-030: subscribing to external tasks for: $subscriptions" }

      subscriptions
        .forEach { subscription ->
          // this is a job to subscribe to.
          externalTaskClient
            .subscribe(subscription.taskDescriptionKey)
            .lockDuration(lockDuration)
            .handler { externalTask, externalTaskService ->
              if (subscription.matches(externalTask)) {
                subscriptionRepository.activateSubscriptionForTask(externalTask.id, subscription)

                val variables = externalTask.allVariables.filterBySubscription(subscription)

                try {
                  logger.debug { "PROCESS-ENGINE-C7-REMOTE-031: delivering service task ${externalTask.id}." }
                  subscription.action.accept(externalTask.toTaskInformation(), variables)
                  logger.debug { "PROCESS-ENGINE-C7-REMOTE-032: successfully delivered service task ${externalTask.id}." }
                } catch (e: Exception) {
                  val jobRetries: Int = externalTask.retries ?: retries
                  logger.error { "PROCESS-ENGINE-C7-REMOTE-033: failing delivering task ${externalTask.id}: ${e.message}" }
                  externalTaskService.handleFailure(
                    externalTask.id,
                    "Error delivering external task",
                    e.message,
                    jobRetries - 1,
                    retryTimeout
                  )
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = externalTask.id)
                  logger.error { "PROCESS-ENGINE-C7-REMOTE-034: successfully failed delivering task ${externalTask.id}: ${e.message}" }
                }
              } else {
                // put it back
                externalTaskService.handleFailure(
                  externalTask.id,
                  "PROCESS-ENGINE-C7-REMOTE-031: No matching handler",
                  "",
                  externalTask.retries, // no changes on retries
                  retryTimeout
                )
              }
            }
            .forSubscription(subscription)
            .open()
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C7-REMOTE-035: external tasks subscribing is disabled because of no active subscriptions" }
    }
  }

  /*
   * Additional restrictions to check.
   * The activated job can be completed by the Subscription strategy and is correct type (topic).
   */
  private fun TaskSubscriptionHandle.matches(externalTask: ExternalTask): Boolean {
    return this.taskType == TaskType.EXTERNAL && (
      this.taskDescriptionKey == null || this.taskDescriptionKey == externalTask.topicName
      )  && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == externalTask.executionId
        CommonRestrictions.ACTIVITY_ID -> it.value == externalTask.activityId
        CommonRestrictions.BUSINESS_KEY -> it.value == externalTask.businessKey
        CommonRestrictions.TENANT_ID -> it.value == externalTask.tenantId
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == externalTask.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_KEY -> it.value == externalTask.processDefinitionKey
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == externalTask.processDefinitionId
        CommonRestrictions.PROCESS_DEFINITION_VERSION_TAG -> it.value == externalTask.processDefinitionVersionTag
        else -> false
      }
    }

    // FIXME: analyze this! check restrictions, etc..
  }

  private fun TopicSubscriptionBuilder.forSubscription(subscription: TaskSubscriptionHandle): TopicSubscriptionBuilder {

    // FIXME -> more limitations....

    return if (subscription.payloadDescription != null && subscription.payloadDescription!!.isNotEmpty()) {
      this.variables(*subscription.payloadDescription!!.toTypedArray())
    } else {
      this
    }
    // FIXME -> consider complex tent filtering
  }
}
