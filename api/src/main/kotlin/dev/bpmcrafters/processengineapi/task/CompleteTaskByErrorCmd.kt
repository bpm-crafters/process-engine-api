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
) : PayloadSupplier by payloadSupplier {
  /**
   * Creates the complete command for a given task id and payload.
   * @param taskId id of the task to complete.
   * @param errorCode error.
   * @param errorMessage Optional details.
   * @param payload payload to use.
   */
  constructor(taskId: String, errorCode: String, errorMessage: String?, payload: Map<String, Any>) : this(
    taskId = taskId,
    errorCode = errorCode,
    errorMessage = errorMessage,
    payloadSupplier = PayloadSupplier { payload }
  )

  /**
   * Creates the complete command for a given task id without payload.
   * @param taskId id of the task to complete.
   * @param errorCode error.
   * @param errorMessage Optional details.
   */
  constructor(taskId: String, errorCode: String, errorMessage: String?) : this(taskId, errorCode, errorMessage, mapOf() )

}
