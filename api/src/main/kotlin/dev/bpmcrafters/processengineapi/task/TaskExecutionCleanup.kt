package dev.bpmcrafters.processengineapi.task

/**
 * Cleanup after a task handler ran, returned by [TaskExecutionObserver.onStart] and run after the handler returned or threw.
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
