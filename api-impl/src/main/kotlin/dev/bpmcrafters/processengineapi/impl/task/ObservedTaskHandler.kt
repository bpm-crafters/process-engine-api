package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskInformation

/**
 * Task handler that lets [TaskExecutionObserver]s watch each execution of the given handler.
 * Starts the observers before the handler, reports anything thrown by the handler and runs their cleanups afterwards.
 * What the handler throws is rethrown unchanged.
 * @param handler handler to observe.
 * @param observers observers in notification order; their cleanups run in reverse order.
 * @since 1.8
 */
class ObservedTaskHandler(
  private val handler: TaskHandler,
  observers: List<TaskExecutionObserver>
) : TaskHandler {

  private val notifier = TaskExecutionNotifier(observers)

  override fun accept(task: TaskInformation, payload: Map<String, Any?>) {
    val cleanups = notifier.start(task)
    try {
      handler.accept(task, payload)
    } catch (e: Throwable) {
      notifier.outcome(task.taskId, TaskExecutionOutcome.Thrown(e))
      throw e
    } finally {
      notifier.cleanUp(task.taskId, cleanups)
    }
  }
}
