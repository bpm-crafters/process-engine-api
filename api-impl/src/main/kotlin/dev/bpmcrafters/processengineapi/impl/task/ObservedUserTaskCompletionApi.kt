package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskType
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import java.util.concurrent.CompletableFuture

/**
 * User task completion API that reports each completion and BPMN error to [TaskExecutionObserver]s as [TaskType.USER].
 * Observers are notified on the calling thread before delegating, so they see the requested outcome, not the engine's answer.
 * @param delegate completion API of the adapter.
 * @param observers observers in notification order.
 * @since 1.8
 */
class ObservedUserTaskCompletionApi(
  private val delegate: UserTaskCompletionApi,
  observers: List<TaskExecutionObserver>
) : UserTaskCompletionApi {

  private val notifier = TaskExecutionNotifier(observers, TaskType.USER)

  override fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty> {
    notifier.outcome(cmd.taskId, TaskExecutionOutcome.Completed)
    return delegate.completeTask(cmd)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): CompletableFuture<Empty> {
    notifier.outcome(cmd.taskId, TaskExecutionOutcome.BpmnError(cmd.errorCode))
    return delegate.completeTaskByError(cmd)
  }
}
