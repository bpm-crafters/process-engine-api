package dev.bpmcrafters.processengineapi.task

/**
 * Adds cross-cutting concerns (logging, tracing, auditing) around every [TaskHandler] execution of
 * a subscription, without replacing the delivery layer or using AOP.
 *
 * Runs in the delivery thread, inside the delivery's try/catch, for every [TaskType]. Filter on
 * [TaskHandlerInterceptorContext.taskType] if needed.
 *
 * @since 1.8
 */
fun interface TaskHandlerInterceptor {
  /**
   * Call [TaskHandlerInterceptorChain.proceed] to run the next interceptor and finally the handler.
   * Skipping the call skips the handler.
   *
   * An exception thrown before the handler returned acts like a handler exception and consumes a
   * retry, so throw it only to veto the execution. An exception thrown afterwards is deliberately
   * only logged and does not affect the task.
   *
   * A handler may report a failure or BPMN error instead of throwing. `proceed` then returns
   * normally, and its [TaskHandlerOutcome] tells what happened.
   * @param context context of the intercepted execution.
   * @param chain chain to proceed with.
   */
  fun intercept(context: TaskHandlerInterceptorContext, chain: TaskHandlerInterceptorChain)
}
