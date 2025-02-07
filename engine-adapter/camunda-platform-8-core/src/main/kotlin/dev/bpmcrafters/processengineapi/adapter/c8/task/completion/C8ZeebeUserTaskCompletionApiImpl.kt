package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.camunda.zeebe.client.ZeebeClient
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

class C8ZeebeUserTaskCompletionApiImpl(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskCompletionApi {

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C8-012: completing user task ${cmd.taskId}." }
    zeebeClient
      .newCompleteCommand(cmd.taskId.toLong())
      .variables(cmd.get())
      .send()
      .join()
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      logger.debug { "PROCESS-ENGINE-C8-013: successfully completed user task ${cmd.taskId}." }
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    zeebeClient
      .newThrowErrorCommand(cmd.taskId.toLong())
      .errorCode(cmd.errorCode)
      .variables(cmd.get())
      .send()
      .join()
    logger.debug { "PROCESS-ENGINE-C8-013: throwing error ${cmd.errorCode} in user task ${cmd.taskId}." }
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C8-013: successfully thrown error ${cmd.errorCode} in user task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
