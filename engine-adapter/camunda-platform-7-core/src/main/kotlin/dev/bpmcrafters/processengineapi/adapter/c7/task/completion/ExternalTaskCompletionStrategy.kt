package dev.bpmcrafters.processengineapi.adapter.c7.task.completion

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import org.camunda.bpm.engine.ExternalTaskService
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Strategy for completing external tasks using Camunda externalTaskService Java API.
 */
class ExternalTaskCompletionStrategy(
  private val workerId: String = UUID.randomUUID().toString(),
  private val externalTaskService: ExternalTaskService,
  private val subscriptionRepository: SubscriptionRepository
) : CompletionStrategy {

  companion object {
    private val SUPPORTED_TASK_TYPES = arrayOf("service")

    fun supports(restrictions: Map<String, String>): Boolean {
      return restrictions.containsKey(CommonRestrictions.TASK_TYPE) && SUPPORTED_TASK_TYPES.contains(restrictions[CommonRestrictions.TASK_TYPE])
    }
  }

  override fun getSupportedRestrictions(): Set<String> {
    return setOf(CommonRestrictions.TASK_TYPE)
  }

  override fun supports(restrictions: Map<String, String>, taskDescriptionKey: String?): Boolean {
    return supports(restrictions)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Unit> {
    externalTaskService.complete(
      cmd.taskId,
      workerId,
      cmd.get()
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)
    return CompletableFuture.completedFuture(null)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Unit> {
    externalTaskService.handleBpmnError(
      cmd.taskId,
      workerId,
      cmd.error
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)
    return CompletableFuture.completedFuture(null)
  }
}
