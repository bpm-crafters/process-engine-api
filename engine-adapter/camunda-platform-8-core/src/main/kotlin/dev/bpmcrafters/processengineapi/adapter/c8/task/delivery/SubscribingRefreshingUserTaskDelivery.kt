package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.c8.task.SubscribingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
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
    logger.info { "[USER TASK DELIVERY] Subscribing for user tasks." }
    subscriptionRepository
      .getTaskSubscriptions()
      .filter { it.taskType == TaskType.USER }
      .forEach { subscription ->
        // this is a job to subscribe to.
        zeebeClient
          .newWorker()
          .jobType(ZEEBE_USER_TASK)
          .handler { client, job ->
            if (subscription.matches(job)) {
              logger.debug { "Retrieved user task ${job.key}" }
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
              zeebeClient.newUpdateRetriesCommand(job).retries(job.retries + 1)
              client.newFailCommand(job.key)
            }
          }
          .name(workerId)
          .timeout(userTaskLockTimeoutMs * TIMEOUT_FACTOR)
          .forSubscription(subscription)
          // FIXME -> metrics to setup
          .open()
          .let {
            jobWorkerRegistry + (subscription.taskDescriptionKey to it)
          }
      }
  }

  override fun refresh() {
    logger.trace { "[USER TASK DELIVERY] Refreshing user tasks." }
    subscriptionRepository
      .getDeliveredTaskIds(TaskType.USER)
      .forEach { taskId ->
        try {
          logger.trace { "Extending job $taskId..." }
          zeebeClient
            .newUpdateTimeoutCommand(taskId.toLong())
            .timeout(userTaskLockTimeoutMs)
            .send()
            .join()
          logger.trace { "Extended job $taskId." }
        } catch (e: ClientStatusException) {
          when (e.statusCode) {
            Status.Code.NOT_FOUND -> {
              subscriptionRepository.getActiveSubscriptionForTask(taskId)?.let {
                logger.trace { "User task is gone, sending termination to the handler." }
                it.termination.accept(taskId)
                subscriptionRepository.deactivateSubscriptionForTask(taskId)
                logger.trace { "Termination sent to handler and user task is removed." }
              }
            }

            else -> logger.error(e) { "Error extending job $taskId." }
          }
        }
      }
  }

  override fun unsubscribe(taskSubscription: TaskSubscription) {
    if (taskSubscription is TaskSubscriptionHandle) {
      logger.debug { "Unsubscribe from user task: ${taskSubscription.taskDescriptionKey}" }
      jobWorkerRegistry[taskSubscription.taskDescriptionKey]?.close()
    }
  }

  fun unsubscribeAll() {
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
