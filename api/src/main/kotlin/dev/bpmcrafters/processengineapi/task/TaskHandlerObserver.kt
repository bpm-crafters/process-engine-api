package dev.bpmcrafters.processengineapi.task

/**
 * Observes every [TaskHandler] execution of a subscription, for cross-cutting concerns such as
 * logging, tracing or auditing, without replacing the delivery layer or using AOP. It is not an
 * engine listener such as a Camunda `ExecutionListener` or `TaskListener`.
 *
 * An observer cannot affect the task: the handler always runs exactly once, and everything an
 * observer throws is only logged, except a [VirtualMachineError]. Observers run synchronously in the
 * delivery thread, for every [TaskType], so keep them cheap and non-blocking.
 *
 * The outcome is only reported if the adapter also wraps its completion API in
 * `ObserverAwareServiceTaskCompletionApi` (module `process-engine-api-impl`); otherwise it is always
 * [TaskHandlerOutcome.Undetermined].
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
   * Called after the handler, even if it threw. Release the resources acquired in [before] in a
   * `finally` block, otherwise they leak into the next execution on this thread.
   * @param context context of the execution.
   * @param state state returned by [before].
   * @param outcome outcome reported by the handler.
   * @param error exception thrown by the handler, if any. It is passed on to the delivery afterwards.
   */
  fun after(context: TaskHandlerContext, state: S, outcome: TaskHandlerOutcome, error: Throwable?)
}
