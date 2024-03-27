package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import mu.KLogging
import org.camunda.bpm.engine.TaskService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Implementation using Camunda taskService Java API for completion of user tasks.
 * @since 0.0.1
 */
class C7UserTaskCompletionApiImpl(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    taskService.complete(
      cmd.taskId,
      cmd.get()
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    taskService.handleBpmnError(
      cmd.taskId,
      cmd.errorCode
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
