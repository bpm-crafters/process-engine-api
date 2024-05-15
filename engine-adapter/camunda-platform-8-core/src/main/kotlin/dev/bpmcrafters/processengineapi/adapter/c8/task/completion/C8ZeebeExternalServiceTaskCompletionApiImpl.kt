package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import io.camunda.zeebe.client.ZeebeClient
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class C8ZeebeExternalServiceTaskCompletionApiImpl(
  private val zeebeClient: ZeebeClient,
  private val subscriptionRepository: SubscriptionRepository
) : ExternalTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    zeebeClient
      .newCompleteCommand(cmd.taskId.toLong())
      .variables(cmd.get())
      .send()
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
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
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
    zeebeClient
      .newFailCommand(cmd.taskId.toLong())
    subscriptionRepository.deactivateSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
