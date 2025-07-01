package dev.bpmcrafters.processengineapi.task

/**
 * Change assignment of the task command.
 * @since 1.3
 */
abstract class ChangeAssignmentModifyTaskCmd(
  /**
   * Unique task id.
   */
  override val taskId: String,
) : ModifyTaskCmd {

  /**
   * Set a new assignee.
   * @since 1.3
   */
  class AssignTaskCmd(
    override val taskId: String,
    val assignee: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Set new assignee to none.
   * @since 1.3
   */
  class UnassignTaskCmd(
    override val taskId: String
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Add candidate group to the list.
   * @since 1.3
   */
  class AddCandidateGroupTaskCmd(
    override val taskId: String,
    val candidateGroup: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Add candidate user to the list.
   * @since 1.3
   */
  class AddCandidateUserTaskCmd(
    override val taskId: String,
    val candidateUser: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Remove candidate group from the list.
   * @since 1.3
   */
  class RemoveCandidateGroupTaskCmd(
    override val taskId: String,
    val candidateGroup: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Remove candidate user from the list.
   * @since 1.3
   */
  class RemoveCandidateUserTaskCmd(
    override val taskId: String,
    val candidateUser: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Set (=replace) the list of candidate groups.
   * @since 1.3
   */
  class SetCandidateGroupsTaskCmd(
    override val taskId: String,
    val candidateGroups: List<String>,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Set (=replace) the list of candidate users.
   * @since 1.3
   */
  class SetCandidateUsersTaskCmd(
    override val taskId: String,
    val candidateUsers: List<String>,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Clear the list of candidate users.
   * @since 1.3
   */
  class ClearCandidateUsersTaskCmd(
    override val taskId: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

  /**
   * Clear the list of candidate groups.
   * @since 1.3
   */
  class ClearCandidateGroupsTaskCmd(
    override val taskId: String,
  ): ChangeAssignmentModifyTaskCmd(taskId)

}
