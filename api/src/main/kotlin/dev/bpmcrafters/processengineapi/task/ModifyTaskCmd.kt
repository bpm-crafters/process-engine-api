package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.ExecutionModeAware

/**
 * Captures the intent of task modification.
 * @since 1.3
 */
interface ModifyTaskCmd : ExecutionModeAware {
  val taskId: String
}
