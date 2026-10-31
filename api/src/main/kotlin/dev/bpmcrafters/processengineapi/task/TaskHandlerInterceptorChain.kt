package dev.bpmcrafters.processengineapi.task

/**
 * Chain to continue an intercepted [TaskHandler] execution.
 * @since 1.8
 */
fun interface TaskHandlerInterceptorChain {
  /**
   * Proceeds with the next interceptor and finally the wrapped handler.
   * Must be called in the interceptor's thread. Each call invokes the handler again, which may
   * finish the task twice.
   * @return outcome reported by the wrapped handler.
   */
  fun proceed(): TaskHandlerOutcome
}
