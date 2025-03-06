package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import org.camunda.bpm.client.task.ExternalTaskService as ClientExternalTaskService

private val logger = KotlinLogging.logger {}

/**
 * External task completion API implementation using official client-based external task service.
 * @param externalTaskService external task service provided by the official Camunda Platform 7 client
 * @param subscriptionRepository repository for subscriptions.
 */
class C7RemoteClientServiceTaskCompletionApiImpl(
    private val externalTaskService: ClientExternalTaskService,
    private val subscriptionRepository: SubscriptionRepository,
    private val failureRetrySupplier: FailureRetrySupplier
) : ServiceTaskCompletionApi {

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-006: completing service task ${cmd.taskId}." }
    externalTaskService
      .complete(
        cmd.taskId,
        cmd.get(),
        mapOf()
      )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-REMOTE-007: successfully completed service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-008: throwing error ${cmd.errorCode} in service task ${cmd.taskId}." }
    externalTaskService
      .handleBpmnError(
        cmd.taskId,
        cmd.errorCode,
        cmd.errorMessage,
        cmd.get()
      )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-REMOTE-009: successfully thrown error in service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-010: failing service task ${cmd.taskId}." }
    val (retries, retryTimeoutInSeconds) = failureRetrySupplier.apply(cmd.taskId)
    externalTaskService
      .handleFailure(
        cmd.taskId,
        cmd.reason,
        cmd.errorDetails,
        retries,
        retryTimeoutInSeconds
      )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-REMOTE-011: successfully failed service task ${cmd.taskId} handling." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
