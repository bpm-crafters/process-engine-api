package dev.bpmcrafters.processengineapi

/**
 * Command execution mode.
 * @since 1.6
 */
enum class ExecutionMode {
  /**
   * Alias for async.
   */
  DEFAULT,

  /**
   * Default async execution mode.
   */
  ASYNC,

  /**
   * Synchronous execution mode, adapter should use a specific thread pool
   * to run the same thread and tx as the caller.
   */
  SYNC,

  /**
   * Special execution mode for preflight checks.
   */
  PREFLIGHT_CHECK
}
