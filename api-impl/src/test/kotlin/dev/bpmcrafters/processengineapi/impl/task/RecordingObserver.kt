package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskExecutionCleanup
import dev.bpmcrafters.processengineapi.task.TaskInformation

internal class RecordingObserver(private val name: String, private val recordedSteps: MutableList<String>) : TaskExecutionObserver {

  override fun onStart(task: TaskInformation): TaskExecutionCleanup {
    recordedSteps.add("$name start ${task.taskId}")
    return TaskExecutionCleanup { recordedSteps.add("$name cleanup ${task.taskId}") }
  }

  override fun onOutcome(taskId: String, outcome: TaskExecutionOutcome) {
    recordedSteps.add("$name outcome $taskId $outcome")
  }
}
