package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import io.camunda.zeebe.client.ZeebeClient
import mu.KLogging
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class C8ZeebeExternalServiceTaskCompletionApiImpl(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository,
  private val failureRetrySupplier: FailureRetrySupplier
) : ServiceTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C8-008: completing service task ${cmd.taskId}." }
    zeebeClient
      .newCompleteCommand(cmd.taskId.toLong())
      .variables(cmd.get())
      .send()
      .join()
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C8-009: successfully completed service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C8-008: throwing error ${cmd.errorCode} in service task ${cmd.taskId}." }
    zeebeClient
      .newThrowErrorCommand(cmd.taskId.toLong())
      .errorCode(cmd.errorCode)
      .variables(cmd.get())
      .send()
      .join()
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      logger.debug { "PROCESS-ENGINE-C8-009: successfully thrown error ${cmd.errorCode} in service task ${cmd.taskId}." }
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
    val (retries, retriesTimeout) = failureRetrySupplier.apply(cmd.taskId)
    zeebeClient
      .newFailCommand(cmd.taskId.toLong())
      .retries(retries)
      .retryBackoff(Duration.ofSeconds(retriesTimeout))
      .send()
      .join()
    logger.debug { "PROCESS-ENGINE-C8-010: failing service task ${cmd.taskId}." }
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C8-011: successfully failed service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
