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
) {
  companion object {
    const val REASON = "reason"

    const val CREATE = "create"
    const val ASSIGN = "assign"
    const val UPDATE = "update"
    const val COMPLETE = "complete"
    const val DELETE = "delete"
  }

  /**
   * Creates a new task information with specified reason.
   * @param reason reason to set.
   * @return task information.
   */
  fun withReason(reason: String): TaskInformation {
    return this.copy(meta = meta + (REASON to reason))
  }

  /**
   * Cleans up th reason, if previously available.
   * @return task information without reason set.
   */
  fun cleanupReason(): TaskInformation {
    return this.copy(meta = meta.filterNot { it.key == REASON })
  }
}
