package dev.bpmcrafters.processengineapi.task

/**
 * Read-only context of an observed [TaskHandler] execution. The handler receives the original
 * arguments, so changing this context never reaches it. The payload is a view, not a copy: values
 * inside it are shared with the handler and must not be mutated, and changes the handler makes to a
 * mutable payload map are visible in [TaskHandlerObserver.after].
 * @since 1.8
 */
class TaskHandlerContext(
  /**
   * Information about the task being delivered, with an unmodifiable view of its meta.
   */
  val taskInformation: TaskInformation,
  /**
   * Unmodifiable view of the payload variables delivered to the handler.
   */
  val payload: Map<String, Any?>,
  /**
   * Task description key of the subscription, if any.
   */
  val taskDescriptionKey: String?,
  /**
   * Type of the observed task.
   */
  val taskType: TaskType
) {
  override fun toString(): String =
    "TaskHandlerContext(taskId=${taskInformation.taskId}, taskDescriptionKey=$taskDescriptionKey, taskType=$taskType)"
}
