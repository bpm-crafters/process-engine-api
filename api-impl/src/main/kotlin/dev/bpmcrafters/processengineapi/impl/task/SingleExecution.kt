package dev.bpmcrafters.processengineapi.impl.task

/**
 * Runs the [execution] at most once and remembers whether it ran and whether it failed.
 * A failure of the execution is rethrown unchanged.
 */
internal class SingleExecution(
  private val execution: Runnable,
  private val onRepeatedRun: () -> Unit
) : Runnable {

  private val result = lazy { runCatching { execution.run() } }

  val hasRun: Boolean
    get() = result.isInitialized()

  val hasFailed: Boolean
    get() = hasRun && result.value.isFailure

  override fun run() {
    if (hasRun) {
      onRepeatedRun()
    } else {
      result.value.getOrThrow()
    }
  }
}
