package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.camunda.tasklist.CamundaTaskListClient
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class C8TaskListClientUserTaskCompletionApiImpl(
  private val taskListClient: CamundaTaskListClient,
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C8-006: completing service task ${cmd.taskId}." }
    taskListClient.completeTask(cmd.taskId, cmd.get())
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.debug { "PROCESS-ENGINE-C8-007: successfully completed service task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    TODO("Not supported by task list client")
  }
}
