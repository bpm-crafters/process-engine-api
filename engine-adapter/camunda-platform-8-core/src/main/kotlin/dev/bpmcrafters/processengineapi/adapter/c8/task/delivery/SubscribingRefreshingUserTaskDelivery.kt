package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c8.task.SubscribingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.command.ActivateJobsCommandStep1.ActivateJobsCommandStep3
import io.camunda.zeebe.client.api.command.ClientStatusException
import io.camunda.zeebe.client.api.command.StreamJobsCommandStep1.StreamJobsCommandStep3
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobWorker
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3
import io.camunda.zeebe.protocol.Protocol
import io.grpc.Status
import mu.KLogging

class SubscribingRefreshingUserTaskDelivery(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val workerId: String,
  private val userTaskLockTimeoutMs: Long,
) : SubscribingUserTaskDelivery, RefreshableDelivery {

  companion object : KLogging()

  private var jobWorkerRegistry: Map<String, JobWorker> = emptyMap()

  fun subscribe() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C8-040: subscribing user tasks for subscriptions: $subscriptions" }
      subscriptions
        .filter { it.taskType == TaskType.USER }
        .forEach { activeSubscription ->
          // this is a job to subscribe to.
          val subscribedJobWorker = zeebeClient
            .newWorker()
            .jobType(Protocol.USER_TASK_JOB_TYPE)
            .handler { _, job -> consumeActivatedJob(activeSubscription, job, zeebeClient) }
            .maxJobsActive(Integer.MAX_VALUE)
            .name(workerId)
            .timeout(userTaskLockTimeoutMs)
            .streamEnabled(false)
            .forSubscription(activeSubscription)
            // FIXME -> metrics to setup
            .open()

          // add to registry, to be able to close worker and stop receiving updates on unsubscribe
          jobWorkerRegistry + (activeSubscription.taskDescriptionKey to subscribedJobWorker)
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C8-046: not subscribing for user tasks, no active subscription found." }
    }
  }

  private fun consumeActivatedJob(activeSubscription: TaskSubscriptionHandle, job: ActivatedJob, zeebeClient: ZeebeClient) {
    if (activeSubscription.matches(job)) {
      subscriptionRepository.activateSubscriptionForTask("${job.key}", activeSubscription)
      val variables = job.variablesAsMap.filterBySubscription(activeSubscription)
      try {
        logger.debug { "PROCESS-ENGINE-C8-041: Delivering user task ${job.key}." }
        activeSubscription.action.accept(job.toTaskInformation(), variables)
        logger.debug { "PROCESS-ENGINE-C8-042: Successfully delivered user task ${job.key}." }
      } catch (e: Exception) {
        logger.error { "PROCESS-ENGINE-C8-043: Failed to deliver user task ${job.key}: ${e.message}" }
        zeebeClient
          .newFailCommand(job.key)
          .retries(job.retries)
          .send()
          .join() // could not deliver
        subscriptionRepository.deactivateSubscriptionForTask(taskId = "${job.key}")
      }
    } else {
      // put it back
      logger.trace { "PROCESS-ENGINE-C8-044: Received user task ${job.key} not matching subscriptions, returning it." }
      zeebeClient
        .newFailCommand(job.key)
        .retries(job.retries + 1)
        .send()
        .join()
      logger.trace { "PROCESS-ENGINE-C8-045: Successfully returned user task ${job.key} not matching subscriptions." }
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
  private fun TaskSubscriptionHandle.matches(job: ActivatedJob): Boolean {
    return this.taskType == TaskType.USER
      && (this.taskDescriptionKey == null || this.taskDescriptionKey == job.elementId)
      && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == "${job.elementInstanceKey}"
        CommonRestrictions.ACTIVITY_ID -> it.value == job.elementId
        CommonRestrictions.TENANT_ID -> it.value == job.tenantId
        CommonRestrictions.PROCESS_DEFINITION_KEY -> it.value == job.bpmnProcessId
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == "${job.processDefinitionKey}"
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == "${job.processInstanceKey}"
        else -> false
      }
    }
    // job.customHeaders // FIXME: analyze this! make sure we reflect the subscription restrictions
  }

  private fun ActivateJobsCommandStep3.forSubscription(subscription: TaskSubscriptionHandle): ActivateJobsCommandStep3 {
    // FIXME -> tenantId
    // FIXME -> more to setup from props
    return if (subscription.payloadDescription != null && subscription.payloadDescription!!.isNotEmpty()) {
      this.fetchVariables(subscription.payloadDescription!!.toList())
    } else {
      this
    }
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

  private fun StreamJobsCommandStep3.forSubscription(subscription: TaskSubscriptionHandle): StreamJobsCommandStep3 {
    // FIXME -> tenantId
    // FIXME -> more to setup from props
    return if (subscription.payloadDescription != null && subscription.payloadDescription!!.isNotEmpty()) {
      this.fetchVariables(subscription.payloadDescription!!.toList())
    } else {
      this
    }
  }

}
