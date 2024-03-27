package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Strategy for completing external tasks using Camunda externalTaskService Java API.
 */
class C7RemoteServiceExternalTaskCompletionApiImpl(
  private val workerId: String,
  private val externalTaskService: ExternalTaskService,
  private val subscriptionRepository: SubscriptionRepository
) : ExternalTaskCompletionApi {

  companion object : KLogging()

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    externalTaskService.complete(
      cmd.taskId,
      workerId,
      cmd.get()
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.info { "Successfully completed external task ${cmd.taskId}." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    externalTaskService.handleBpmnError(
      cmd.taskId,
      workerId,
      cmd.errorCode
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.info { "Completed external task ${cmd.taskId} with error." }
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun failTask(cmd: FailTaskCmd): Future<Empty> {
    externalTaskService.handleFailure(
      cmd.taskId,
      workerId,
      cmd.reason,
      cmd.errorDetails,
      100, // FIXME -> how to get those, they are only in the job
      1000 // FIXME -> retry timeout from props?
    )
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      termination.accept(cmd.taskId)
      logger.info { "Failure occurred on external task ${cmd.taskId} handling." }
    }
    return CompletableFuture.completedFuture(Empty)
  }
}
