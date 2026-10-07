package dev.bpmcrafters.processengineapi.task

/**
 * Outcome of an intercepted [TaskHandler] execution, as reported by the handler via the
 * [ServiceTaskCompletionApi] in the thread executing the handler.
 * @since 1.7
 */
sealed interface TaskHandlerOutcome {

  /**
   * The handler completed the task.
   */
  data object Completed : TaskHandlerOutcome

  /**
   * The handler completed the task by throwing a BPMN error.
   */
  data class CompletedByError(
    /**
     * Error code.
     */
    val errorCode: String,
    /**
     * Optional error details.
     */
    val errorMessage: String?
  ) : TaskHandlerOutcome

  /**
   * The handler failed the task because of a technical failure.
   */
  data class Failed(
    /**
     * Failure reason.
     */
    val reason: String,
    /**
     * Optional failure details.
     */
    val errorDetails: String?
  ) : TaskHandlerOutcome

  /**
   * The handler returned without reporting an outcome in the executing thread, for example because
   * the task is completed later or by another thread, or because the handler was not invoked.
   */
  data object Undetermined : TaskHandlerOutcome
}
