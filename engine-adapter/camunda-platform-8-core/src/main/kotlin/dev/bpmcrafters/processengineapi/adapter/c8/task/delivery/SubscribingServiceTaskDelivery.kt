package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3
import mu.KLogging

/**
 * Uses task subscription available in the repository to subscribe to zeebe.
 */
class SubscribingServiceTaskDelivery(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val workerId: String
) {

  companion object: KLogging()

  fun subscribe() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C8-050: subscribing service tasks for subscriptions: $subscriptions" }
      subscriptions
        .filter { it.taskType == TaskType.EXTERNAL }
        .forEach { activeSubscription ->
          // this is a job to subscribe to.
          zeebeClient
            .newWorker()
            .jobType(activeSubscription.taskDescriptionKey)
            .handler { client, job ->
              if (activeSubscription.matches(job)) {
                subscriptionRepository.activateSubscriptionForTask("${job.key}", activeSubscription)
                val variables = job.variablesAsMap.filterBySubscription(activeSubscription)
                try {
                  logger.debug { "PROCESS-ENGINE-C8-051: Delivering service task ${job.key}." }
                  activeSubscription.action.accept(job.toTaskInformation(), variables)
                  logger.debug { "PROCESS-ENGINE-C8-052: Successfully delivered service task ${job.key}." }
                } catch (e: Exception) {
                  logger.error { "PROCESS-ENGINE-C8-051: Failing to deliver service task ${job.key}: ${e.message}." }
                  client.newFailCommand(job.key).retries(job.retries).send().join() // could not deliver
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = "${job.key}")
                  logger.error { "PROCESS-ENGINE-C8-052: Successfully failed to deliver service task ${job.key}: ${e.message}." }
                }
              } else {
                // put it back
                // TODO: check this, is it ok to put the job this way back?
                logger.trace { "PROCESS-ENGINE-C8-053: Received service task ${job.key} not matching subscriptions, returning it." }
                client.newFailCommand(job.key).retries(job.retries + 1).send().join()
                logger.trace { "PROCESS-ENGINE-C8-045: Successfully returned service task ${job.key} not matching subscriptions." }
              }
            }
            .name(workerId)
            .forSubscription(activeSubscription)
            // FIXME -> metrics to setup
            .open()
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C8-050: Not subscribing service tasks for subscriptions. No subscriptions found." }
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
      this
        .fetchVariables(subscription.payloadDescription!!.toList())
    } else {
      this
    }
  }
}
