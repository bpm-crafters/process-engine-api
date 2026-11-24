package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler
import dev.bpmcrafters.processengineapi.task.TaskType

/**
 * Termination handler that reports each termination to [TaskExecutionObserver]s before calling the given handler.
 * @param handler termination handler to observe.
 * @param taskType type of the terminated tasks.
 * @param observers observers in notification order.
 * @since 1.8
 */
class ObservedTaskTerminationHandler(
  private val handler: TaskTerminationHandler,
  taskType: TaskType,
  observers: List<TaskExecutionObserver>
) : TaskTerminationHandler {

  private val notifier = TaskExecutionNotifier(observers, taskType)

  override fun accept(task: TaskInformation) {
    notifier.termination(task)
    handler.accept(task)
  }
}
