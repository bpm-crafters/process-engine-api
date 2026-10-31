package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import java.util.concurrent.CompletableFuture

/**
 * Decorates a [ServiceTaskCompletionApi] and reports each accepted request of a task handler as
 * [TaskHandlerOutcome] to the running [TaskHandlerInterceptor]s, regardless of the returned future.
 *
 * Reports only calls made in the handler's thread, and nothing if the call throws synchronously.
 * @since 1.8
 */
class InterceptorAwareServiceTaskCompletionApi(
  private val delegate: ServiceTaskCompletionApi
) : ServiceTaskCompletionApi {

  override fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty> =
    delegate.completeTask(cmd).also {
      TaskHandlerOutcomeRecorder.record(cmd.taskId, TaskHandlerOutcome.Completed)
    }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): CompletableFuture<Empty> =
    delegate.completeTaskByError(cmd).also {
      TaskHandlerOutcomeRecorder.record(cmd.taskId, TaskHandlerOutcome.CompletedByError(cmd.errorCode, cmd.errorMessage))
    }

  override fun failTask(cmd: FailTaskCmd): CompletableFuture<Empty> =
    delegate.failTask(cmd).also {
      TaskHandlerOutcomeRecorder.record(cmd.taskId, TaskHandlerOutcome.Failed(cmd.reason, cmd.errorDetails))
    }
}
