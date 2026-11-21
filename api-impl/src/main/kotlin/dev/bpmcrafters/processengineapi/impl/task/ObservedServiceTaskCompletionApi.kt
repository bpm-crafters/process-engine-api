package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import java.util.concurrent.CompletableFuture

/**
 * Completion API that reports each completion, BPMN error and failure to [TaskExecutionObserver]s.
 * Observers are notified on the calling thread before delegating, so they see the requested outcome, not the engine's answer.
 * @param delegate completion API of the adapter.
 * @param observers observers in notification order.
 * @since 1.8
 */
class ObservedServiceTaskCompletionApi(
  private val delegate: ServiceTaskCompletionApi,
  observers: List<TaskExecutionObserver>
) : ServiceTaskCompletionApi {

  private val notifier = TaskExecutionNotifier(observers)

  override fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty> {
    notifier.outcome(cmd.taskId, TaskExecutionOutcome.Completed)
    return delegate.completeTask(cmd)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): CompletableFuture<Empty> {
    notifier.outcome(cmd.taskId, TaskExecutionOutcome.BpmnError(cmd.errorCode))
    return delegate.completeTaskByError(cmd)
  }

  override fun failTask(cmd: FailTaskCmd): CompletableFuture<Empty> {
    notifier.outcome(cmd.taskId, TaskExecutionOutcome.Failed(cmd.reason, cmd.errorDetails))
    return delegate.failTask(cmd)
  }
}
