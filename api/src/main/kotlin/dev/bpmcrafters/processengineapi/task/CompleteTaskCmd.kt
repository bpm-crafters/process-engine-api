package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to complete the task.
 * @since 0.0.1
 */
open class CompleteTaskCmd(
  /**
   * Unique task id.
   */
  val taskId: String,
  /**
   * Payload supplier.
   */
  private val payloadSupplier: PayloadSupplier
) : PayloadSupplier by payloadSupplier
