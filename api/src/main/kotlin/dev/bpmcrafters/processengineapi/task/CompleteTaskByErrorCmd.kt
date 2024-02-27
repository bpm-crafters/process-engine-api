package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier

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
  val error: String,
  /**
   * Payload supplier.
   */
  private val payloadSupplier: PayloadSupplier
) : PayloadSupplier by payloadSupplier
