package dev.bpmcrafters.processengineapi.task

/**
 * Read-only context of an intercepted [TaskHandler] execution.
 * @since 1.8
 */
data class TaskHandlerInterceptorContext(
  /**
   * Information about the task being delivered.
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
   * Type of the intercepted task.
   */
  val taskType: TaskType
)
