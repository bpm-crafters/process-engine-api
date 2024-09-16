package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.c8.task.SubscribingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.command.ClientStatusException
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobWorker
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3
import io.grpc.Status
import mu.KLogging

class SubscribingRefreshingUserTaskDelivery(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val workerId: String,
  private val userTaskLockTimeoutMs: Long,
) : SubscribingUserTaskDelivery, RefreshableDelivery {

  companion object : KLogging() {
    const val ZEEBE_USER_TASK = "io.camunda.zeebe:userTask"
    const val TIMEOUT_FACTOR = 2L
  }

  private var jobWorkerRegistry: Map<String, JobWorker> = emptyMap()

  fun subscribe() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C8-040: subscribing user tasks for subscriptions: $subscriptions" }
      subscriptions
        .filter { it.taskType == TaskType.USER }
        .forEach { activeSubscription ->
          // this is a job to subscribe to.
          zeebeClient
            .newWorker()
            .jobType(ZEEBE_USER_TASK)
            .handler { client, job ->
              if (activeSubscription.matches(job)) {
                subscriptionRepository.activateSubscriptionForTask("${job.key}", activeSubscription)
                val variables = job.variablesAsMap.filterBySubscription(activeSubscription)
                try {
                  logger.debug { "PROCESS-ENGINE-C8-041: Delivering user task ${job.key}." }
                  activeSubscription.action.accept(job.toTaskInformation(), variables)
                  logger.debug { "PROCESS-ENGINE-C8-042: Successfully delivered user task ${job.key}." }
                } catch (e: Exception) {
                  logger.error { "PROCESS-ENGINE-C8-043: Failed to deliver user task ${job.key}: ${e.message}" }
                  client
                    .newFailCommand(job.key)
                    .retries(job.retries)
                    .send()
                    .join() // could not deliver
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = "${job.key}")
                }
              } else {
                // put it back
                logger.trace { "PROCESS-ENGINE-C8-044: Received user task ${job.key} not matching subscriptions, returning it." }
                client
                  .newFailCommand(job.key)
                  .retries(job.retries + 1)
                  .send()
                  .join()
                logger.trace { "PROCESS-ENGINE-C8-045: Successfully returned user task ${job.key} not matching subscriptions." }
              }
            }
            .name(workerId)
            .timeout(userTaskLockTimeoutMs * TIMEOUT_FACTOR)
            .forSubscription(activeSubscription)
            // FIXME -> metrics to setup
            .open()
            .let {
              jobWorkerRegistry + (activeSubscription.taskDescriptionKey to it)
            }
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C8-046: not subscribing for user tasks, no active subscription found." }
    }
  }

  override fun refresh() {
    val subscriptions = subscriptionRepository.getDeliveredTaskIds(TaskType.USER)
    logger.trace { "PROCESS-ENGINE-C8-047: refreshing user tasks for subscriptions: $subscriptions" }
    if (subscriptions.isNotEmpty()) {
      subscriptions.forEach { taskId ->
        try {
          logger.trace { "PROCESS-ENGINE-C8-048: Extending job timout for user task $taskId..." }
          zeebeClient
            .newUpdateTimeoutCommand(taskId.toLong())
            .timeout(userTaskLockTimeoutMs)
            .send()
            .join()
          logger.trace { "PROCESS-ENGINE-C8-049: Extended job timout for user task $taskId." }
        } catch (e: ClientStatusException) {
          when (e.statusCode) {
            Status.Code.NOT_FOUND -> {
              subscriptionRepository.getActiveSubscriptionForTask(taskId)?.let {
                logger.trace { "PROCESS-ENGINE-C8-050: User task is gone, sending termination to the handler." }
                it.termination.accept(taskId)
                subscriptionRepository.deactivateSubscriptionForTask(taskId)
                logger.trace { "PROCESS-ENGINE-C8-051: Termination sent to handler and user task is removed." }
              }
            }

            else -> logger.error(e) { "PROCESS-ENGINE-C8-052: Error extending job $taskId, ${e.message}." }
          }
        }
      }
    } else {
      logger.trace { "PROCESS-ENGINE-C8-053: not subscribing for user tasks, no active subscription found." }
    }
  }

  override fun unsubscribe(taskSubscription: TaskSubscription) {
    if (taskSubscription is TaskSubscriptionHandle) {
      logger.debug { "PROCESS-ENGINE-C8-054: Unsubscribe from user task: ${taskSubscription.taskDescriptionKey}" }
      jobWorkerRegistry[taskSubscription.taskDescriptionKey]?.close()
    }
  }

  fun unsubscribeAll() {
    logger.debug { "PROCESS-ENGINE-C8-055: Unsubscribe all user tasks." }
    jobWorkerRegistry.forEach { (_, job) -> job.close() }
  }

  /*
   * Additional restrictions to check.
   */
  @Suppress("UNUSED_PARAMETER")
  private fun TaskSubscriptionHandle.matches(job: ActivatedJob): Boolean {
    return this.taskType == TaskType.USER
    // job.customHeaders // FIXME: analyze this! make sure we reflect the subscription restrictions
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
