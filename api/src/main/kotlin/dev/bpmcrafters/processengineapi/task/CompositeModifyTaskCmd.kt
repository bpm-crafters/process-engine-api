package dev.bpmcrafters.processengineapi.task

/**
 * Composite modification command to perform multiple modifications on a single task.
 * @since 1.3
 */
class CompositeModifyTaskCmd(
  override val taskId: String,
  val commands: List<ModifyTaskCmd>
) : ModifyTaskCmd {

  init {
      require(commands.all { it.taskId == this.taskId }) { "All nested commands must have the same taskId." }
  }
}
