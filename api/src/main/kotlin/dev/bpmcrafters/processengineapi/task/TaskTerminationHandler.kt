package dev.bpmcrafters.processengineapi.task

/**
 * Handler that is invoked if the task is deleted after the task has been assigned to the handler.
 * @since 0.0.1
 */
interface TaskTerminationHandler {

  companion object {
    @JvmStatic
    val empty: TaskTerminationHandler = object : TaskTerminationHandler {}
  }

  /**
   * Invoked if the task has been terminated after it is assigned to the handler.
   * @param taskId task id of terminated task.
   */
  fun terminated(taskId: String) {

  }
}
