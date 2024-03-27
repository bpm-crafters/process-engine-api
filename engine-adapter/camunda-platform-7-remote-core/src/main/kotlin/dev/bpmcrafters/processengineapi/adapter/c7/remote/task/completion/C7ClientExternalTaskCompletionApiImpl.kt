package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import org.camunda.bpm.client.task.ExternalTaskService as ClientExternalTaskService

/**
 * External task completion API implementation using official client-based external task service.
 * @param externalTaskService external task service provided by the official Camunda Platform 7 client
 * @param subscriptionRepository repository for subscriptions.
 */
class C7ClientExternalTaskCompletionApiImpl(
  private val externalTaskService: ClientExternalTaskService,
  private val subscriptionRepository: SubscriptionRepository
) : ExternalTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    externalTaskService
      .complete(
        cmd.taskId,
        cmd.get(),
        mapOf()
      )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    externalTaskService
      .handleBpmnError(
        cmd.taskId,
        cmd.errorCode,
        "",
        cmd.get()
      )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
    externalTaskService
      .handleFailure(
        cmd.taskId,
        cmd.reason,
        cmd.errorDetails,
        100,
        1000
      )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
