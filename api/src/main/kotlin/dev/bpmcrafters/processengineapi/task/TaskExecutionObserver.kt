package dev.bpmcrafters.processengineapi.task

/**
 * Observes the lifecycle of delivered tasks, e.g. for audit logs, tracing or MDC.
 * It covers external service tasks and user tasks; every callback names the [TaskType], so an observer can filter.
 * An observer only watches: it cannot skip, repeat or change a delivery, its variables or its result.
 * Exceptions thrown by an observer are logged and ignored, so the task is handled as without observers.
 * @since 1.8
 */
interface TaskExecutionObserver {

  /**
   * Called on the delivering thread right before the task handler is invoked.
   * Whatever is set up here, e.g. a current span or MDC values, stays on this thread until the returned cleanup runs.
   * The cleanup runs after the handler returned or threw.
   * For user tasks, the handler only receives the task, e.g. for a task list; the user completes it later.
   * It may be called several times for the same task, e.g. when a user task is assigned or updated.
   * The adapter may name the reason in [TaskInformation.meta] under [TaskInformation.REASON], e.g. [TaskInformation.CREATE], [TaskInformation.ASSIGN] or [TaskInformation.UPDATE].
   * It sees the task as the adapter delivered it, because it runs before any handler: an update that
   * [dev.bpmcrafters.processengineapi.task.support.UserTaskSupport] passes to its handlers as [TaskInformation.ASSIGN] arrives here with the adapter's reason, e.g. [TaskInformation.UPDATE].
   * @param task information about the delivered task.
   * @param taskType type of the delivered task.
   * @return cleanup that undoes what was set up.
   */
  fun onStart(task: TaskInformation, taskType: TaskType): TaskExecutionCleanup = TaskExecutionCleanup.NONE

  /**
   * Called when the task is completed or failed via [ServiceTaskCompletionApi] or [UserTaskCompletionApi], or an exception escapes the handler.
   * It reports the requested outcome, not whether the engine accepted it.
   * User tasks only report [TaskExecutionOutcome.Completed], [TaskExecutionOutcome.BpmnError] and [TaskExecutionOutcome.Thrown].
   * A task may report no, one or several outcomes, e.g. a failure followed by a thrown exception.
   * What [onStart] set up is only current if the handler calls the completion API on the delivering thread before it returns.
   * @param taskId id of the task.
   * @param taskType type of the task.
   * @param outcome outcome of the task.
   */
  fun onOutcome(taskId: String, taskType: TaskType, outcome: TaskExecutionOutcome) {}

  /**
   * Called when the adapter reports that a delivered task is gone, e.g. completed, cancelled or deleted.
   * The adapter may name the reason in [TaskInformation.meta] under [TaskInformation.REASON], e.g. [TaskInformation.COMPLETE] or [TaskInformation.DELETE].
   * An adapter may report it right after a completion via [ServiceTaskCompletionApi] or [UserTaskCompletionApi], so a completed task may report both [onOutcome] and [onTermination].
   * It may also run when the adapter detects that the task is gone in the engine, then usually on another thread than [onStart].
   * When and whether it is called depends on the adapter and the engine.
   * @param task information about the terminated task.
   * @param taskType type of the terminated task.
   */
  fun onTermination(task: TaskInformation, taskType: TaskType) {}
}
