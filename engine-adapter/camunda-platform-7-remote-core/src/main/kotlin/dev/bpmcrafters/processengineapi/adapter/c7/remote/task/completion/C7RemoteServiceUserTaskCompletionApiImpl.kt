package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.TaskService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

/**
 * Strategy for completing user tasks using Camunda taskService Java API.
 */
class C7RemoteServiceUserTaskCompletionApiImpl(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskCompletionApi {

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-011: completing user task ${cmd.taskId}." }
    taskService.complete(
      cmd.taskId,
      cmd.get()
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-REMOTE-012: successfully completed user task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-013: throwing error on user task ${cmd.taskId}." }
    taskService.handleBpmnError(
      cmd.taskId,
      cmd.errorCode
    )
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C7-REMOTE-014: successfully thrown error on user task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
