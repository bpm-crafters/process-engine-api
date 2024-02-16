package dev.bpmcrafters.processengineapi.process

/**
 * Information about the process instance.
 * @since 0.0.1
 */
data class ProcessInformation(
  /**
   * Reference to the instance.
   */
  val instanceId: String,
  /**
   * Additional metadata about started instance.
   */
  val meta: Map<String, String>
)
