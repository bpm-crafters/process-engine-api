package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskType

/**
 * Task subscription handle.
 * @since 0.0.1
 */
data class TaskSubscriptionHandle(
  /**
   * Type of the task.
   */
  val taskType: TaskType,
  /**
   * Set of variable names delivered as payload.
   * If `null`, no limitation is provided and the entire payload is delivered.
   * If `empty set`, no variables will be delivered.
   * If contain any values, only variable names contained in the set will be delivered.
   */
  val payloadDescription: Set<String>?,
  /**
   * Restricts the subscription.
   */
  val restrictions: Map<String, String>,
  /**
   * Optionally restricts the subscription to a task description key. By default, we consider the element ID from BPMN XML to be the description key.
   * Adapter may implement this differently.
   */
  val taskDescriptionKey: String?,
  /**
   * Action handler to execute if the task is delivered.
   */
  val action: TaskHandler,
  /**
   * Termination callback to signal that the task is gone.
   */
  val termination: TaskTerminationHandler
) : TaskSubscription {
  override fun toString(): String {
    return "TaskSubscriptionHandle(taskType=$taskType, payloadDescription=$payloadDescription, restrictions=$restrictions, taskDescriptionKey=$taskDescriptionKey"
  }
}

