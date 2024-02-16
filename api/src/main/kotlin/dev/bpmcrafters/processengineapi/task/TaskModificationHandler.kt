package dev.bpmcrafters.processengineapi.task

/**
 * Handler that is invoked if the task is modified after the task has been assigned to the handler.
 * @since 0.0.1
 */
interface TaskModificationHandler {

  companion object {
    @JvmStatic
    val empty: TaskModificationHandler = object : TaskModificationHandler {}
  }

  /**
   * Invoked if the task is modified after the task is assigned to the handler.
   * @param taskInformation information of the modified task.
   * @param payload payload variables.
   */
  fun modified(taskInformation: TaskInformation, payload: Map<String, Any>) {

  }

  /**
   * Invoked if the task has been terminated after it is assigned to the handler.
   * @param taskId task id of terminated task.
   */
  fun terminated(taskId: String) {

  }
}
