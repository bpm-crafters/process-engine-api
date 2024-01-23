package dev.bpmcrafters.processengineapi.task

/**
 * Command to complete the task by error.
 * @since 0.0.1
 */
open class CompleteTaskByErrorCmd(
  /**
   * Task id.
   */
  val taskId: String,
  /**
   * Error.
   */
  val error: String
)
