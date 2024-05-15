package dev.bpmcrafters.processengineapi.task

/**
 * Represents task information.
 * @since 0.0.1
 */
data class TaskInformation(
  /**
   * Reference to the instance.
   */
  val taskId: String,
  /**
   * Additional metadata about the task.
   */
  val meta: Map<String, String>
)
