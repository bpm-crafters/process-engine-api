package dev.bpmcrafters.processengineapi.task

import java.time.OffsetDateTime

/**
 * Change date-related properties of the task command.
 * @since 1.6
 */
abstract class ChangeDatesModifyTaskCmd(
  /**
   * Unique task id.
   */
  override val taskId: String,
) : ModifyTaskCmd {

  /**
   * Set a new due date.
   * @since 1.6
   */
  class SetDueDateTaskCmd(
    override val taskId: String,
    val dueDate: OffsetDateTime,
  ) : ChangeDatesModifyTaskCmd(taskId)

  /**
   * Clear the due date.
   * @since 1.6
   */
  class ClearDueDateTaskCmd(
    override val taskId: String,
  ) : ChangeDatesModifyTaskCmd(taskId)

  /**
   * Set a new follow-up date.
   * @since 1.6
   */
  class SetFollowUpDateTaskCmd(
    override val taskId: String,
    val followUpDate: OffsetDateTime,
  ) : ChangeDatesModifyTaskCmd(taskId)

  /**
   * Clear the follow-up date.
   * @since 1.6
   */
  class ClearFollowUpDateTaskCmd(
    override val taskId: String,
  ) : ChangeDatesModifyTaskCmd(taskId)

}
