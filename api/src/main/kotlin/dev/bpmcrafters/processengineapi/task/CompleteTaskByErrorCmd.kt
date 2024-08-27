package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to complete the task by BPMN error.
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
  val errorCode: String,
  /**
   * Optional error details.
   */
  val errorMessage: String? = null,
  /**
   * Payload supplier.
   */
  private val payloadSupplier: PayloadSupplier
) : PayloadSupplier by payloadSupplier
