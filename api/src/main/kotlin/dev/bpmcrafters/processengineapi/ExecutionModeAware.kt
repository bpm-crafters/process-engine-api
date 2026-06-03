package dev.bpmcrafters.processengineapi

/**
 * Interface to retrieve information of execution mode.
 * @since 1.6
 */
interface ExecutionModeAware {
  /**
   * Retrieves execution mode.
   * @return Execution mode.
   */
  fun executionMode(): ExecutionMode = ExecutionMode.DEFAULT
}
