package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.ExternalTaskService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

/**
 * Strategy for completing external tasks using Camunda externalTaskService Java API.
 */
class C7ServiceTaskCompletionApiImpl(
    private val workerId: String,
    private val externalTaskService: ExternalTaskService,
    private val subscriptionRepository: SubscriptionRepository,
    private val failureRetrySupplier: FailureRetrySupplier
) : ServiceTaskCompletionApi {

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {

    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-006: completing service task ${cmd.taskId}." }
    externalTaskService.complete(
      cmd.taskId,
      workerId,
      cmd.get()
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-007: successfully completed service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-008: throwing error ${cmd.errorCode} in service task ${cmd.taskId}." }
    externalTaskService.handleBpmnError(
      cmd.taskId,
      workerId,
      cmd.errorCode,
      cmd.errorMessage,
      cmd.get()
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-009: successfully thrown error in service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-010: failing service task ${cmd.taskId}." }
    val (retries, retryTimeoutInSeconds) = failureRetrySupplier.apply(cmd.taskId)
    externalTaskService.handleFailure(
      cmd.taskId,
      workerId,
      cmd.reason,
      cmd.errorDetails,
      retries,
      retryTimeoutInSeconds
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-011: successfully failed service task ${cmd.taskId} handling." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
