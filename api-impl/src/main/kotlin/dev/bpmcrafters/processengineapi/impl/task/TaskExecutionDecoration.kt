package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionDecorator
import dev.bpmcrafters.processengineapi.task.TaskInformation
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections

private val logger = KotlinLogging.logger {}

/**
 * Runs the per-task work of a delivery with the [TaskExecutionDecorator]s around it, the first one outermost.
 * Adapters create one instance and call [run] around the whole per-task work, including its failure handling.
 * The work always runs exactly once; a decorator that fails, skips or repeats it is only logged.
 * @param decorators decorators in execution order.
 * @since 1.8
 */
class TaskExecutionDecoration(private val decorators: List<TaskExecutionDecorator>) {

  /**
   * Exceptions thrown by the work pass through unchanged.
   * @param task information about the delivered task.
   * @param payload payload delivered to the handler.
   * @param execution per-task work of the delivery.
   */
  fun run(task: TaskInformation, payload: Map<String, Any?>, execution: Runnable) {
    val singleTaskExecution = SingleTaskExecution(execution) { warnRepeatedRun(task) }

    try {
      val decoratedExecution = decorate(readOnly(task), Collections.unmodifiableMap(payload), singleTaskExecution)
      decoratedExecution.run()
    } catch (e: Exception) {
      if (singleTaskExecution.hasFailed) throw e
      logDecoratorFailure(task, e)
    }

    if (!singleTaskExecution.hasRun) {
      warnSkippedRun(task)
      singleTaskExecution.run()
    }
  }

  private fun decorate(task: TaskInformation, payload: Map<String, Any?>, execution: Runnable): Runnable =
    decorators.foldRight(execution) { decorator, inner ->
      try {
        decorator.decorate(task, payload, inner)
      } catch (e: Exception) {
        logDecoratorFailure(task, e)
        inner
      }
    }

  private fun readOnly(task: TaskInformation) = task.copy(meta = Collections.unmodifiableMap(task.meta))

  private fun warnRepeatedRun(task: TaskInformation) =
    logger.warn { "PROCESS-ENGINE-API-018: Decorator ran task ${task.taskId} again, ignoring the repeated run." }

  private fun warnSkippedRun(task: TaskInformation) =
    logger.warn { "PROCESS-ENGINE-API-017: Decorators did not run task ${task.taskId}, running it without them." }

  private fun logDecoratorFailure(task: TaskInformation, e: Exception) =
    logger.error(e) { "PROCESS-ENGINE-API-016: Task execution decorator failed for task ${task.taskId}." }

  companion object {
    /**
     * Decoration without decorators, which just runs the work.
     */
    @JvmField
    val NONE = TaskExecutionDecoration(emptyList())
  }
}
