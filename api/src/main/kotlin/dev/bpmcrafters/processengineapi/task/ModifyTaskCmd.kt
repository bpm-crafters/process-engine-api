package dev.bpmcrafters.processengineapi.task

/**
 * Captures the intent of task modification.
 * @since 1.3
 */
interface ModifyTaskCmd {
  val taskId: String
}
