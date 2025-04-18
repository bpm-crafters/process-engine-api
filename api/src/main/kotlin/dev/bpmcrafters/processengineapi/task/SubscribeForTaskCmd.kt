package dev.bpmcrafters.processengineapi.task

import java.util.function.Consumer

/**
 * Command to subscribe to tasks.
 * @since 0.0.1
 */
data class SubscribeForTaskCmd(
  /**
   * Defines a set of restrictions evaluated by the engine adapter.
   */
  val restrictions: Map<String, String>,
  /**
   * Task type.
   */
  val taskType: TaskType,
  /**
   * May refer to BPMN 2.0 attribute `implementation` or `operation[@implementationRef]` or
   * any engine-specific attribute of the task XML-tag. As a fallback an adapter-implementation
   * should also accept a task's `id` attribute, since this the only common attribute in all engines.
   */
  val taskDescriptionKey: String?,
  /**
   * Limitation of the payload variables to be delivered to the action.
   * If empty, no variables should be provided.
   * If non-empty, the variables are limited to those, provided in the list.
   * If null, all variables are provided.
   */
  val payloadDescription: Set<String>? = null,
  /**
   * Action to deliver the task to.
   */
  val action: TaskHandler,
  /**
   * Action to execute if the delivered task is terminated.
   */
  val termination: TaskTerminationHandler
) {
  constructor(
    /**
     * Defines a set of restrictions evaluated by the engine adapter.
     */
    restrictions: Map<String, String>,
    /**
     * Task type.
     */
    taskType: TaskType,
    /**
     * May refer to BPMN 2.0 attribute `implementation` or `operation[@implementationRef]` or
     * any engine-specific attribute of the task XML-tag. As a fallback an adapter-implementation
     * should also accept a task's `id` attribute, since this the only common attribute in all engines.
     */
    taskDescriptionKey: String?,
    /**
     * Limitation of the payload variables to be delivered to the action.
     * If empty, no variables should be provided.
     * If non-empty, the variables are limited to those, provided in the list.
     * If null, all variables are provided.
     */
    payloadDescription: Set<String>? = null,
    /**
     * Action to deliver the task to.
     */
    action: TaskHandler,
    /**
     * Action to execute if the delivered task is terminated.
     */
    termination: Consumer<String>
  ) : this(restrictions, taskType, taskDescriptionKey, payloadDescription, action, TaskTerminationHandler { taskInformation ->
    termination.accept(taskInformation.taskId)
  })
}
