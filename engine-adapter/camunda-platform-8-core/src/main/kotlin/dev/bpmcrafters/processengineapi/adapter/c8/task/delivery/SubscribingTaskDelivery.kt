package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.TaskInformation
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob

/**
 * Uses task subscription available in the repository to subscribe to zeebe.
 */
class SubscribingTaskDelivery(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository
) {

  fun subscribe() {
    
    subscriptionRepository.getTaskSubscriptions()
      .forEach { subscription ->
        zeebeClient
          .newWorker()
          .jobType(subscription.taskDescriptionKey)
          .handler { client, job ->
            subscriptionRepository.activateSubscriptionForTask("${job.key}", subscription)

            job.customHeaders // FIXME: analyze this! user/service task, etc..

            val variables = if (subscription.payloadDescription.isEmpty()) {
              job.variablesAsMap
            } else {
              job.variablesAsMap.filter { subscription.payloadDescription.contains(it.key) }
            }
            try {
              subscription.action.accept(job.toTaskInformation(), variables)
            } catch (e: Exception) {
              client.newFailCommand(job.key) // could not deliver
            }
          }
      }
  }

  private fun ActivatedJob.toTaskInformation(): TaskInformation = TaskInformation(
    taskId = "${this.key}",
    meta = mapOf() // FIXME
  )
}
