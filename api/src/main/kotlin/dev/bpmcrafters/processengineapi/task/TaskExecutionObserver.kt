package dev.bpmcrafters.processengineapi.task

/**
 * Observes executions of external service tasks, e.g. for audit logs, tracing or MDC.
 * An observer only watches: it cannot skip, repeat or change an execution, its variables or its result.
 * Exceptions thrown by an observer are logged and ignored, so the task executes as without observers.
 * @since 1.8
 */
interface TaskExecutionObserver {

  /**
   * Called on the executing thread right before the task handler is invoked.
   * Whatever is set up here, e.g. a current span or MDC values, stays on this thread until the returned cleanup runs.
   * The cleanup runs after the handler returned or threw.
   * @param task information about the task to execute.
   * @return cleanup that undoes what was set up.
   */
  fun onStart(task: TaskInformation): TaskExecutionCleanup = TaskExecutionCleanup.NONE

  /**
   * Called when [ServiceTaskCompletionApi] is called for the task or an exception escapes the handler.
   * It reports the outcome requested by the handler, not whether the engine accepted it.
   * One execution may report no, one or several outcomes, e.g. a failure followed by a thrown exception.
   * What [onStart] set up is only current if the handler calls [ServiceTaskCompletionApi] on the executing thread before it returns.
   * @param taskId id of the executed task.
   * @param outcome outcome of the execution.
   */
  fun onOutcome(taskId: String, outcome: TaskExecutionOutcome) {}
}
