package dev.bpmcrafters.processengineapi.task

/**
 * Immutable context of an intercepted [TaskHandler] execution.
 * @since 1.7
 */
data class TaskHandlerInterceptorContext(
  /**
   * Information about the task being delivered.
   */
  val taskInformation: TaskInformation,
  /**
   * Payload variables delivered to the handler.
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
