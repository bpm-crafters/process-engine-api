package dev.bpmcrafters.processengineapi.task

/**
 * Cleanup after one task execution, returned by [TaskExecutionObserver.onStart] and run after the execution.
 * It undoes what the observer set up, e.g. it removes MDC values and ends the span.
 * @since 1.8
 */
fun interface TaskExecutionCleanup : AutoCloseable {

  override fun close()

  companion object {
    /**
     * Cleanup that does nothing.
     */
    @JvmField
    val NONE = TaskExecutionCleanup {}
  }
}
