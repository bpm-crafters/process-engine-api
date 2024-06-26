package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3

/**
 * Uses task subscription available in the repository to subscribe to zeebe.
 */
class SubscribingServiceTaskDelivery(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val workerId: String
) {

  fun subscribe() {
    subscriptionRepository
      .getTaskSubscriptions()
      .filter { it.taskType == TaskType.EXTERNAL }
      .forEach { subscription ->
        // this is a job to subscribe to.
        zeebeClient
          .newWorker()
          .jobType(subscription.taskDescriptionKey)
          .handler { client, job ->
            if (subscription.matches(job)) {
              subscriptionRepository.activateSubscriptionForTask("${job.key}", subscription)
              val variables = if (subscription.payloadDescription == null) {
                job.variablesAsMap
              } else {
                if (subscription.payloadDescription!!.isEmpty()) {
                  mapOf()
                } else {
                  job.variablesAsMap.filter { subscription.payloadDescription!!.contains(it.key) }
                }
              }
              try {
                subscription.action.accept(job.toTaskInformation(), variables)

              } catch (e: Exception) {
                client.newFailCommand(job.key) // could not deliver
                subscriptionRepository.deactivateSubscriptionForTask(taskId = "${job.key}")
              }
            } else {
              // put it back
              // TODO: check this, is it ok to put the job this way back?
              zeebeClient.newUpdateRetriesCommand(job).retries(job.retries + 1)
              client.newFailCommand(job.key)
            }
          }
          .name(workerId)
          .forSubscription(subscription)
          // FIXME -> metrics to setup
          .open()
      }
  }

  /*
   * Additional restrictions to check.
   * The activated job can be completed by the Subscription strategy and is correct type (topic).
   */
  @Suppress("UNUSED_PARAMETER")
  private fun TaskSubscriptionHandle.matches(job: ActivatedJob): Boolean {
    return this.taskType == TaskType.EXTERNAL
    // job.customHeaders // FIXME: analyze this! user/service task, etc..
  }

  private fun JobWorkerBuilderStep3.forSubscription(subscription: TaskSubscriptionHandle): JobWorkerBuilderStep3 {
    // FIXME -> tenantId
    // FIXME -> more to setup from props
    return if (subscription.payloadDescription != null && subscription.payloadDescription!!.isNotEmpty()) {
      this.fetchVariables(subscription.payloadDescription!!.toList())
    } else {
      this
    }
  }
}
