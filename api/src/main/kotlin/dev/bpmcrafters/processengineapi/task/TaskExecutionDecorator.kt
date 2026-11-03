package dev.bpmcrafters.processengineapi.task

/**
 * Decorates the execution of an external service task, e.g. for tracing, MDC, logging or metrics.
 * Similar to Spring's `TaskDecorator`, not an engine listener such as Camunda's `ExecutionListener`.
 *
 * `execution` is the whole per-task work of the delivery:
 * the [TaskHandler] call and the delivery's failure handling.
 * Code around `execution.run()` therefore also covers the delivery's failure logs.
 *
 * A decorator cannot change the task, as task information and payload are read-only.
 * Call `execution.run()` exactly once; the adapter ensures this and only logs misbehaviour.
 * Decorators run synchronously in the delivery thread, so keep them cheap.
 * @since 1.8
 */
fun interface TaskExecutionDecorator {
  /**
   * Decorates the execution of one task.
   * @param task information about the delivered task, as an unmodifiable view.
   * @param payload payload delivered to the handler, as an unmodifiable view.
   * @param execution per-task work of the delivery. Run it exactly once.
   * @return runnable that runs [execution] with the decoration around it.
   */
  fun decorate(task: TaskInformation, payload: Map<String, Any?>, execution: Runnable): Runnable
}
