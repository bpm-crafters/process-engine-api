package dev.bpmcrafters.processengineapi.process

/**
 * Response for the process start.
 * @since 0.0.1
 */
data class StartProcessResponse(
  /**
   * Reference to the instance.
   */
  val instanceId: String,
  /**
   * Additional metadata about started instance.
   */
  val meta: Map<String, String>
)
