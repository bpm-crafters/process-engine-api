package dev.bpmcrafters.processengineapi.task.support

import dev.bpmcrafters.processengineapi.task.TaskInformation

/**
 * Detects assignment changes.
 * @since 1.1
 */
interface AssignmentDetector {
  /**
   * Detects if assignment has changed from old to new task information.
   * @param oldTaskInformation old task information and payload.
   * @param newTaskInformation new task information and payload.
   * @return true, if assignment change is detected. Defaults to `false`.
   */
  fun hasChangedAssignment(oldTaskInformation: Pair<TaskInformation, Map<String, Any?>>, newTaskInformation: Pair<TaskInformation, Map<String, Any?>>): Boolean = false
}
