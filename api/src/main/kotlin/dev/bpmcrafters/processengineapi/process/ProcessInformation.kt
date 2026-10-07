package dev.bpmcrafters.processengineapi.process

/**
 * Information about the process instance.
 * @since 0.0.1
 */
data class ProcessInformation(
  /**
   * Reference to the instance.
   *
   * When an engine puts [dev.bpmcrafters.processengineapi.CommonRestrictions.PROCESS_INSTANCE_ID] into the
   * meta of a task of this instance, it is the same value. A caller can rely on that to match a delivered
   * task to the process instance it started. An engine which uses a different value in one of the two places
   * has a bug.
   */
  val instanceId: String,
  /**
   * Additional metadata about started instance.
   */
  val meta: Map<String, String>
)
