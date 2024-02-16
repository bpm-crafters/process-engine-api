package dev.bpmcrafters.processengineapi.adapter.c7.task.completion

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.c7.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import org.camunda.bpm.engine.TaskService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Strategy for completing user tasks using Camunda taskService Java API.
 */
class UserTaskCompletionStrategy(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository
) : CompletionStrategy {

  companion object {
    private val SUPPORTED_TASK_TYPES = arrayOf("user")
    fun supports(restrictions: Map<String, String>): Boolean {
      return restrictions.containsKey(CommonRestrictions.TASK_TYPE) && SUPPORTED_TASK_TYPES.contains(restrictions[CommonRestrictions.TASK_TYPE])
    }
  }

  override fun getSupportedRestrictions(): Set<String> {
    return setOf(CommonRestrictions.TASK_TYPE)
  }

  override fun supports(restrictions: Map<String, String>, taskDescriptionKey: String?): Boolean {
    return Companion.supports(restrictions)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    taskService.complete(
      cmd.taskId,
      cmd.get()
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    taskService.handleBpmnError(
      cmd.taskId,
      cmd.error
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)
    return CompletableFuture.completedFuture(Empty)
  }
}
