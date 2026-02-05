package dev.bpmcrafters.processengineapi.task.support

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskInformation

/**
 * Composite task handler.
 * @since 0.1.2
 * @param handlers handlers to include.
 */
class CompositeTaskHandler(
    private val handlers: MutableList<TaskHandler> = mutableListOf(),
) : TaskHandler {
  /**
   * Adds handler.
   * @param handler handler to add.
   */
  fun addHandler(handler: TaskHandler) {
    handlers.add(handler)
  }

  /**
   * Creates a new composite task handler out of current adding the given task handler.
   * @param handler handler to add.
   * @return composite handler.
   */
  fun withHandler(handler: TaskHandler): CompositeTaskHandler {
    return CompositeTaskHandler(handlers.plus(handler).toMutableList())
  }

  override fun accept(taskInformation: TaskInformation, payload: Map<String, Any?>) {
    handlers.fold( Pair(taskInformation, payload)) { params, handler -> handler.process(params) }
  }

}
