package dev.bpmcrafters.processengineapi.task

data class SubscribeForTaskCmd(
  var taskType: TaskType,
  val taskDefinitionKey: String,
  val payloadDescription: Set<String>,
  val action: (taskId: String, payload: Map<String, Any>) -> Unit
)
