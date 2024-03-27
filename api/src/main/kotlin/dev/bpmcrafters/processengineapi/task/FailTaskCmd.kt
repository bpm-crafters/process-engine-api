package dev.bpmcrafters.processengineapi.task

/**
 * Command indicating a failure during task processing.
 * @since 0.0.1
 */
open class FailTaskCmd(
  /**
   * Unique task id.
   */
  val taskId: String,
  /**
   * Failure reason.
   */
  val reason: String,
  /**
   * Optional failure details.
   */
  val errorDetails: String?
)
