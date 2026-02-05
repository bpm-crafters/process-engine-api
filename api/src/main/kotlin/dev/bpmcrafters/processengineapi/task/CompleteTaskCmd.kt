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
) : PayloadSupplier by payloadSupplier {

  /**
   * Creates the complete command for a given task id and payload.
   * @param taskId id of the task to complete.
   * @param payload payload to use.
   */
  constructor(taskId: String, payload: Map<String, Any?>) : this(taskId, PayloadSupplier { payload } )

  /**
   * Creates the complete command for a given task id without payload.
   * @param taskId id of the task to complete.
   */
  constructor(taskId: String) : this(taskId, mapOf() )
}
