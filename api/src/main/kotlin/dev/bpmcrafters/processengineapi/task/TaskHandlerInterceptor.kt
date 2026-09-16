package dev.bpmcrafters.processengineapi.task

/**
 * Intercepts the execution of a [TaskHandler] of a subscription.
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
   * @param context context of the intercepted execution.
   * @param chain chain to proceed with.
   */
  fun intercept(context: TaskHandlerInterceptorContext, chain: TaskHandlerInterceptorChain)
}
