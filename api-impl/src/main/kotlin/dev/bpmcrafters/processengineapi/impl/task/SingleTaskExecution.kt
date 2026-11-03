package dev.bpmcrafters.processengineapi.impl.task

/**
 * Runs the execution of one delivered task at most once.
 * The task execution is what the adapter delivery does for this task: calling the task handler and handling its failure.
 * Remembers whether the task execution ran and whether it failed; a failure is rethrown unchanged.
 */
internal class SingleTaskExecution(
  private val taskExecution: Runnable,
  private val onRepeatedRun: () -> Unit
) : Runnable {

  private val result = lazy { runCatching { taskExecution.run() } }

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
