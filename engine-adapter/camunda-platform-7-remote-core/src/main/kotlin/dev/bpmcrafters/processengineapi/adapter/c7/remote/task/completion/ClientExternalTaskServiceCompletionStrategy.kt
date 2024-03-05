package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import org.camunda.bpm.client.task.ExternalTaskService as ClientExternalTaskService

/**
 * External task completion strategy using official client-based external task service.
 * @param externalTaskService external task service provided by the official Camunda Platform 7 client
 * @param subscriptionRepository repository for subscriptions.
 */
class ClientExternalTaskServiceCompletionStrategy(
  private val externalTaskService: ClientExternalTaskService,
  private val subscriptionRepository: SubscriptionRepository
) : CompletionStrategy {

  companion object : KLogging() {
    private val SUPPORTED_TASK_TYPES = arrayOf(CommonRestrictions.TASK_TYPE_SERVICE)

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
    externalTaskService
      .complete(
        cmd.taskId,
        cmd.get(),
        mapOf()
      )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      modification.terminated(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    externalTaskService
      .handleBpmnError(
        cmd.taskId,
        cmd.error,
        "",
        cmd.get()
      )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      modification.terminated(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
