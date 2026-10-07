package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome

/**
 * Keeps the [TaskHandlerOutcome] reported for the task currently handled by the executing thread.
 * @since 1.7
 */
internal object TaskHandlerOutcomeRecorder {

  private class Recording(val taskId: String) {
    var outcome: TaskHandlerOutcome = TaskHandlerOutcome.Undetermined
  }

  private val currentRecording = ThreadLocal<Recording?>()

  fun <T> recordingFor(taskId: String, execution: () -> T): T {
    val enclosingRecording = currentRecording.get()
    currentRecording.set(Recording(taskId))
    try {
      return execution()
    } finally {
      if (enclosingRecording == null) currentRecording.remove() else currentRecording.set(enclosingRecording)
    }
  }

  fun record(taskId: String, outcome: TaskHandlerOutcome) {
    currentRecording.get()?.takeIf { it.taskId == taskId }?.outcome = outcome
  }

  fun recordedOutcome(): TaskHandlerOutcome = currentRecording.get()?.outcome ?: TaskHandlerOutcome.Undetermined
}
