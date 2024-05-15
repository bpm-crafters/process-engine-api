package dev.bpmcrafters.processengineapi.adapter.commons.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskType

/**
 * Task subscription handle.
 */
data class TaskSubscriptionHandle(
  val taskType: TaskType,
  val payloadDescription: Set<String>?,
  val restrictions: Map<String, String>,
  val taskDescriptionKey: String?,
  val action: TaskHandler,
  val termination: TaskTerminationHandler
) : TaskSubscription {
  override fun toString(): String {
    return "TaskSubscriptionHandle(taskType=$taskType, payloadDescription=$payloadDescription, restrictions=$restrictions, taskDescriptionKey=$taskDescriptionKey"
  }
}

