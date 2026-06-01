package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.ExecutionModeAware
import java.time.Duration

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
  val errorDetails: String?,
  /**
   * Optional retry count.
   */
  val retryCount: Int?,
  /**
   * Optional retry backoff duration.
   */
  val retryBackoff: Duration?
) : ExecutionModeAware {
  constructor(taskId: String, reason: String, errorDetails: String?)
    : this(taskId = taskId, reason = reason, errorDetails = errorDetails, null, null)
}
