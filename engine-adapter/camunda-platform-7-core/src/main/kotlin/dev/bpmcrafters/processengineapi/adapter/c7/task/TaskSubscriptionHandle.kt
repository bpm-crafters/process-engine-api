package dev.bpmcrafters.processengineapi.adapter.c7.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskSubscription

data class TaskSubscriptionHandle(
  val payloadDescription: Set<String>,
  val action: TaskHandler,
  val restrictions: Map<String, String>,
  val taskDescriptionKey: String?
) : TaskSubscription

