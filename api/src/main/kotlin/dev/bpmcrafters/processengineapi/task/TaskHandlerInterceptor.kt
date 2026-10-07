package dev.bpmcrafters.processengineapi.task

/**
 * Intercepts the execution of a [TaskHandler] of a subscription.
 *
 * Lets you add cross-cutting concerns (logging, tracing, auditing) around every task execution
 * without replacing the delivery layer or resorting to AOP.
 *
 * Interceptors wrap the call `subscription.action.accept(taskInformation, payload)` performed by
 * every delivery. They run in the delivery thread, inside the delivery's try/catch, and apply to
 * every [TaskType]. An interceptor may filter on [TaskHandlerInterceptorContext.taskType].
 *
 * @since 1.7
 */
fun interface TaskHandlerInterceptor {
  /**
   * Intercepts the task handler execution.
   * Call [TaskHandlerInterceptorChain.proceed] to continue with the next interceptor and finally
   * the wrapped handler. Skipping the call prevents the handler from being invoked. An exception
   * thrown here propagates like a handler exception.
   *
   * A handler may report a failure or a BPMN error itself instead of throwing an exception. In this
   * case [TaskHandlerInterceptorChain.proceed] returns normally and the returned [TaskHandlerOutcome]
   * tells what happened.
   * @param context context of the intercepted execution.
   * @param chain chain to proceed with.
   * @return outcome of the execution, usually the one returned by [TaskHandlerInterceptorChain.proceed].
   */
  fun intercept(context: TaskHandlerInterceptorContext, chain: TaskHandlerInterceptorChain): TaskHandlerOutcome
}
