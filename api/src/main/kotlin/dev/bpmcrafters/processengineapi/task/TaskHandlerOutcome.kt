package dev.bpmcrafters.processengineapi.task

/**
 * What a [TaskHandler] requested via the [ServiceTaskCompletionApi] in its executing thread, not
 * whether the engine processed it. Requires a completion API wrapped in
 * `InterceptorAwareServiceTaskCompletionApi`.
 * @since 1.8
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
   * No outcome was reported in the executing thread: the task is finished later or by another
   * thread, is a user task, the completion call threw synchronously, the adapter does not report
   * outcomes, or the handler was not invoked.
   */
  data object Undetermined : TaskHandlerOutcome
}
