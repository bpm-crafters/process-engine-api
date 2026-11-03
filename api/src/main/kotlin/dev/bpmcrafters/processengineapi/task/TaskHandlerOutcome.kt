package dev.bpmcrafters.processengineapi.task

/**
 * What a [TaskHandler] requested via the [ServiceTaskCompletionApi] in its executing thread, not
 * whether the engine processed it. Requires a completion API wrapped in
 * `ObserverAwareServiceTaskCompletionApi` (module `process-engine-api-impl`). From Java, the
 * singletons are `TaskHandlerOutcome.Completed.INSTANCE` and `TaskHandlerOutcome.Undetermined.INSTANCE`.
 * @since 1.8
 */
sealed interface TaskHandlerOutcome {

  /**
   * The handler completed the task.
   */
  data object Completed : TaskHandlerOutcome

  /**
   * The handler completed the task by throwing a BPMN error. Mirrors [CompleteTaskByErrorCmd].
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
   * The handler failed the task because of a technical failure. Mirrors [FailTaskCmd].
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
   * thread, is not finished through a [ServiceTaskCompletionApi] (as user tasks), the completion
   * call threw synchronously, or the adapter does not report outcomes.
   */
  data object Undetermined : TaskHandlerOutcome
}
