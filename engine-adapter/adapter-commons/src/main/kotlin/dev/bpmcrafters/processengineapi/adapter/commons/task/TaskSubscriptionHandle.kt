package dev.bpmcrafters.processengineapi.adapter.commons.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskModificationHandler
import dev.bpmcrafters.processengineapi.task.TaskSubscription

data class TaskSubscriptionHandle(
  val payloadDescription: Set<String>,
  val restrictions: Map<String, String>,
  val taskDescriptionKey: String?,
  val action: TaskHandler,
  val modification: TaskModificationHandler
) : TaskSubscription

