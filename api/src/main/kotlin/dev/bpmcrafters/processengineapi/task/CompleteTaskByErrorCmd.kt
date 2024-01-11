package dev.bpmcrafters.processengineapi.task

class CompleteTaskByErrorCmd(
  taskId: String,
  payload: () -> Map<String, Any>,
  val error: String
) : CompleteTaskCmd(taskId = taskId, payload = payload)
