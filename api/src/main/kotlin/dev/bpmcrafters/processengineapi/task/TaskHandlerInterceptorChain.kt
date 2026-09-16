package dev.bpmcrafters.processengineapi.task

/**
 * Chain to continue an intercepted [TaskHandler] execution.
 * @since 1.7
 */
fun interface TaskHandlerInterceptorChain {
  /**
   * Proceeds with the next interceptor and finally the wrapped handler.
   */
  fun proceed()
}
