package dev.bpmcrafters.processengineapi.task

/**
 * Observes the delivery of external service tasks to a [TaskHandler], for cross-cutting concerns
 * such as logging, tracing or auditing, without replacing the delivery layer or using AOP. It is not
 * an engine listener such as a Camunda `ExecutionListener` or `TaskListener`.
 *
 * The delivery runs observers around its whole per-task work: [before] runs before the handler,
 * [after] once the delivery has finished, including its failure handling. Only `EXTERNAL` tasks are
 * observed.
 *
 * An observer cannot affect the task: the handler always runs exactly once, and everything an
 * observer throws is only logged, except a [VirtualMachineError]. Observers run synchronously in the
 * delivery thread, so keep them cheap and non-blocking.
 *
 * Observers only run with an adapter whose deliveries use `TaskHandlerObservation` (module
 * `process-engine-api-impl`). The outcome of the handler is only known if the adapter also wraps
 * its completion API in `ObserverAwareServiceTaskCompletionApi`; otherwise it is
 * [TaskHandlerOutcome.Undetermined] unless the delivery reports one.
 *
 * @param S state passed from [before] to [after] of the same execution: [Unit] in Kotlin or `Void`
 * (returning `null`) in Java if none is needed.
 * @since 1.8
 */
interface TaskHandlerObserver<S> {
  /**
   * Called before the handler. Acquire resources all or nothing: if this method throws, [after]
   * is not called for this observer.
   * @param context context of the execution.
   * @return state handed to [after].
   */
  fun before(context: TaskHandlerContext): S

  /**
   * Called after the delivery has finished, even if the handler threw. Release the resources acquired in [before] in a
   * `finally` block, otherwise they leak into the next execution on this thread.
   * @param context context of the execution.
   * @param state state returned by [before].
   * @param outcome outcome reported by the handler or the delivery.
   * @param error exception thrown by the handler or the delivery, if any, also if the delivery
   * handled it.
   */
  fun after(context: TaskHandlerContext, state: S, outcome: TaskHandlerOutcome, error: Throwable?)
}
