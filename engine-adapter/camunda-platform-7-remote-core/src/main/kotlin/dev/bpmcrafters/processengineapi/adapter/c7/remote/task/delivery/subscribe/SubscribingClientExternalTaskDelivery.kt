package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.subscribe

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder

/**
 *
 */
class SubscribingClientExternalTaskDelivery(
  private val externalTaskClient: ExternalTaskClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val lockDuration: Long
) {

  fun subscribe() {
    subscriptionRepository.getTaskSubscriptions()
      .forEach { subscription ->
        // this is a job to subscribe to.
        externalTaskClient
          .subscribe(subscription.taskDescriptionKey)
          .lockDuration(lockDuration)
          .handler { externalTask, externalTaskService ->
            if (subscription.matches(externalTask)) {
              subscriptionRepository.activateSubscriptionForTask(externalTask.id, subscription)
              val variables = if (subscription.payloadDescription.isEmpty()) {
                externalTask.allVariables
              } else {
                externalTask.allVariables.filter { subscription.payloadDescription.contains(it.key) }
              }
              try {
                subscription.action.accept(externalTask.toTaskInformation(), variables)
              } catch (e: Exception) {
                externalTaskService.handleFailure(externalTask.id,
                  "Error delivering external task",
                  e.message,
                  externalTask.retries - 1,
                  10
                ) // FIXME -> props  // could not deliver
              }
            } else {
              // put it back
              // TODO: check this, is it ok to put the job this way back?
              externalTaskService.handleFailure(externalTask.id,
                "No matching handler",
                "",
                externalTask.retries,
                10
              ) // FIXME -> props  // could not deliver
            }
          }
          .forSubscription(subscription)
          .open()
      }

  }

  /*
   * Additional restrictions to check.
   * The activated job can be completed by the Subscription strategy and is correct type (topic).
   */
  private fun TaskSubscriptionHandle.matches(externalTask: ExternalTask): Boolean {
    return this.taskType == TaskType.EXTERNAL && (
      this.taskDescriptionKey == null || this.taskDescriptionKey == externalTask.topicName
      )
    // FIXME: analyze this! check restrictions, etc..
  }

  private fun TopicSubscriptionBuilder.forSubscription(subscription: TaskSubscriptionHandle): TopicSubscriptionBuilder {

    // FIXME -> more limitations....

    return if (subscription.payloadDescription.isNotEmpty()) {
      this.variables(*subscription.payloadDescription.toTypedArray())
    } else {
      this
    }
  }
}
