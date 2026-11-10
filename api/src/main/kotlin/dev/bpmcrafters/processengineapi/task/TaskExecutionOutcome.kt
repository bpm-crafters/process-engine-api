package dev.bpmcrafters.processengineapi.task

/**
 * Outcome of one task execution, passed to [TaskExecutionObserver.onOutcome].
 * @since 1.8
 */
sealed interface TaskExecutionOutcome {

  /**
   * The task was completed via [ServiceTaskCompletionApi.completeTask].
   */
  data object Completed : TaskExecutionOutcome

  /**
   * The task was completed by a BPMN error via [ServiceTaskCompletionApi.completeTaskByError].
   * @param errorCode code of the BPMN error.
   */
  data class BpmnError(val errorCode: String) : TaskExecutionOutcome

  /**
   * The task was failed via [ServiceTaskCompletionApi.failTask].
   * @param reason failure reason.
   * @param errorDetails optional failure details.
   */
  data class Failed(val reason: String, val errorDetails: String?) : TaskExecutionOutcome

  /**
   * An exception escaped the task handler.
   * @param cause the escaped exception.
   */
  data class Thrown(val cause: Throwable) : TaskExecutionOutcome
}
