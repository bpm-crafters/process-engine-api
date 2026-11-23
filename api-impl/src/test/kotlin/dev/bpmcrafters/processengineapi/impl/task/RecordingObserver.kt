package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionCleanup
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType

internal class RecordingObserver(private val name: String, private val recordedSteps: MutableList<String>) : TaskExecutionObserver {

  override fun onStart(task: TaskInformation, taskType: TaskType): TaskExecutionCleanup {
    recordedSteps.add("$name start $taskType ${task.taskId}")
    return TaskExecutionCleanup { recordedSteps.add("$name cleanup ${task.taskId}") }
  }

  override fun onOutcome(taskId: String, taskType: TaskType, outcome: TaskExecutionOutcome) {
    recordedSteps.add("$name outcome $taskType $taskId $outcome")
  }

  override fun onTermination(task: TaskInformation, taskType: TaskType) {
    recordedSteps.add("$name termination $taskType ${task.taskId}")
  }
}
