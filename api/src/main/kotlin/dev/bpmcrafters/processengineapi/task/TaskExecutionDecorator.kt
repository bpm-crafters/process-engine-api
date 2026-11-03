package dev.bpmcrafters.processengineapi.task

/**
 * Decorates the execution of an external service task, for cross-cutting concerns such as tracing,
 * MDC, logging or metrics, in the spirit of Spring's `TaskDecorator`.
 *
 * The adapter delivery passes its whole per-task work as [execution]: the call of the [TaskHandler]
 * and the delivery's own failure handling and logging. Code placed around `execution.run()` therefore
 * runs before and after both, and a span or MDC entry opened there also covers the delivery's
 * failure logs.
 *
 * A decorator cannot change the task: it receives read-only views of the task information and the
 * payload. Call `execution.run()` exactly once, in the thread the decorated runnable runs in. The
 * adapter runs the execution once even if a decorator skips, repeats or fails around it, and only
 * logs such misbehaviour. Decorators run synchronously in the delivery thread, so keep them cheap and
 * non-blocking.
 *
 * This is not an engine listener such as a Camunda `ExecutionListener` or `TaskListener`.
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
