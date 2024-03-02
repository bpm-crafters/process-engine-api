package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import io.camunda.tasklist.CamundaTaskListClient
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class UserTaskCompletionStrategy(
  private val taskListClient: CamundaTaskListClient,
  private val subscriptionRepository: SubscriptionRepository
) : CompletionStrategy {

  companion object : KLogging() {
    private val SUPPORTED_TASK_TYPES = arrayOf(CommonRestrictions.TASK_TYPE_USER)

    fun supports(restrictions: Map<String, String>): Boolean {
      return restrictions.containsKey(CommonRestrictions.TASK_TYPE)
        && SUPPORTED_TASK_TYPES.contains(restrictions[CommonRestrictions.TASK_TYPE])
    }
  }

  override fun getSupportedRestrictions(): Set<String> {
    return setOf(CommonRestrictions.TASK_TYPE)
  }

  override fun supports(restrictions: Map<String, String>, taskDescriptionKey: String?): Boolean {
    return supports(restrictions)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    taskListClient
      .completeTask(cmd.taskId, cmd.get())
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      modification.terminated(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {


    TODO("Not implemented yet") // how can this be done?
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      modification.terminated(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
