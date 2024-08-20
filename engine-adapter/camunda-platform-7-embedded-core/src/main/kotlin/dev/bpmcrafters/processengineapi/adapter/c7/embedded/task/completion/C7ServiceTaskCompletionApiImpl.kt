package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Strategy for completing external tasks using Camunda externalTaskService Java API.
 */
class C7ServiceTaskCompletionApiImpl(
  private val workerId: String,
  private val externalTaskService: ExternalTaskService,
  private val subscriptionRepository: SubscriptionRepository,
  private val failureRetrySupplier: FailureRetrySupplier
) : ServiceTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {

    externalTaskService.complete(
      cmd.taskId,
      workerId,
      cmd.get()
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.info { "[PROCESS-ENGINE-C7-EMBEDDED]: Successfully completed external task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    externalTaskService.handleBpmnError(
      cmd.taskId,
      workerId,
      cmd.errorCode
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.info { "[PROCESS-ENGINE-C7-EMBEDDED]: Completed external task ${cmd.taskId} with error." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
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
      logger.info { "[PROCESS-ENGINE-C7-EMBEDDED]: Failure occurred on external task ${cmd.taskId} handling." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
