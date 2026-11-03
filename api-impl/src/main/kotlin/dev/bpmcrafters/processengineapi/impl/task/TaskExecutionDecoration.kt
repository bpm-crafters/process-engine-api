package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionDecorator
import dev.bpmcrafters.processengineapi.task.TaskInformation
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

/**
 * Runs the per-task work of a delivery with the [TaskExecutionDecorator]s around it. Adapters create
 * one instance from the decorators in execution order and call [run] in every delivery of external
 * tasks, around the whole per-task work including its failure handling.
 *
 * The first decorator is the outermost one. The work runs exactly once: a decorator that fails, skips
 * or repeats the work is only logged. An exception thrown by the work itself passes through unchanged;
 * only a [VirtualMachineError] thrown by a decorator propagates as well.
 * @param decorators decorators in execution order.
 * @since 1.8
 */
class TaskExecutionDecoration(decorators: List<TaskExecutionDecorator>) {

  private val decorators = decorators.toList()

  /**
   * Runs the per-task work of a delivery with the decorators around it.
   * @param task information about the delivered task.
   * @param payload payload delivered to the handler.
   * @param execution per-task work of the delivery, including its failure handling.
   */
  fun run(task: TaskInformation, payload: Map<String, Any?>, execution: Runnable) {
    if (decorators.isEmpty()) {
      execution.run()
      return
    }
    val readOnlyTask = task.copy(meta = Collections.unmodifiableMap(task.meta))
    val readOnlyPayload = Collections.unmodifiableMap(payload)
    val work = SingleRun(task.taskId, execution)
    val decorated = decorators.foldRight<TaskExecutionDecorator, Runnable>(work) { decorator, inner ->
      decorateSafely(decorator, readOnlyTask, readOnlyPayload, inner)
    }
    try {
      decorated.run()
    } catch (e: VirtualMachineError) {
      throw e
    } catch (e: Throwable) {
      if (e === work.error) throw e
      logger.error(e) { "PROCESS-ENGINE-API-016: Task execution decorator failed for task ${task.taskId}." }
    }
    if (!work.started) {
      logger.warn { "PROCESS-ENGINE-API-017: Decorators did not run task ${task.taskId}, running it without them." }
      work.run()
    }
  }

  companion object {
    /**
     * Decoration without decorators, which just runs the work.
     */
    @JvmField
    val NONE = TaskExecutionDecoration(emptyList())
  }
}

private fun decorateSafely(
  decorator: TaskExecutionDecorator,
  task: TaskInformation,
  payload: Map<String, Any?>,
  inner: Runnable
): Runnable =
  try {
    decorator.decorate(task, payload, inner)
  } catch (e: VirtualMachineError) {
    throw e
  } catch (e: Throwable) {
    logger.error(e) { "PROCESS-ENGINE-API-016: Task execution decorator failed for task ${task.taskId}." }
    inner
  }

private class SingleRun(private val taskId: String, private val execution: Runnable) : Runnable {
  private val running = AtomicBoolean(false)
  var error: Throwable? = null
    private set
  val started: Boolean
    get() = running.get()

  override fun run() {
    if (!running.compareAndSet(false, true)) {
      logger.warn { "PROCESS-ENGINE-API-018: Decorator ran task $taskId again, ignoring the repeated run." }
      return
    }
    try {
      execution.run()
    } catch (e: Throwable) {
      error = e
      throw e
    }
  }
}
