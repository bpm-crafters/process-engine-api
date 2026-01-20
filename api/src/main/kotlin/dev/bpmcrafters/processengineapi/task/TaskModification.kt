package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier
import dev.bpmcrafters.processengineapi.task.ChangePayloadModifyTaskCmd.*
import java.util.function.Supplier

/**
 * Fluent builder to create composite task modifications..
 * @since 1.3
 */
class TaskModification(private val taskId: String) {

  private val commands = mutableListOf<ModifyTaskCmd>()

  companion object {
    /**
     * Fluent creation of task modification using receiver functions.
     * @param taskId task id.
     * @param block receiver function to construct the composite.
     * @return composite modification command.
     */
    fun taskModification(taskId: String, block: TaskModification.() -> Unit): CompositeModifyTaskCmd {
      val taskModification = TaskModification(taskId)
      taskModification.block()
      return taskModification.build()
    }
  }

  /**
   * Changes the payload.
   * @param payloadSupplier supplier for new payload.
   * @return builder instance.
   */
  fun updatePayload(payloadSupplier: PayloadSupplier) = this.apply {
    commands += UpdatePayloadTaskCmd(taskId, payloadSupplier)
  }

  /**
   * Changes the payload.
   * @param payload new payload.
   * @return builder instance.
   */
  fun updatePayload(payload: Map<String, Any?>) = this.apply {
    commands += UpdatePayloadTaskCmd(taskId, payload)
  }

  /**
   * Clears the payload.
   * @return builder instance.
   */
  fun clearPayload() = this.apply {
    commands += ClearPayloadTaskCmd(taskId)
  }

  /**
   * Deletes (parts of) the payload.
   * @param payloadKeysSupplier supplier for keys to delete.
   * @return builder instance.
   */
  fun deletePayload(payloadKeysSupplier: Supplier<List<String>>) = this.apply {
    commands += DeletePayloadTaskCmd(taskId, payloadKeysSupplier)
  }

  /**
   * Deletes (parts of) the payload.
   * @param payloadKeys keys to delete.
   * @return builder instance.
   */
  fun deletePayload(payloadKeys: List<String>) = this.apply {
    commands += DeletePayloadTaskCmd(taskId, payloadKeys)
  }


  /**
   * Assigns task.
   * @param assignee new assignee.
   * @return builder instance.
   */
  fun assign(assignee: String) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.AssignTaskCmd(taskId, assignee)
  }

  /**
   * Clears assignment.
   * @return builder instance.
   */
  fun unassign() = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.UnassignTaskCmd(taskId)
  }

  /**
   * Adds new candidate user.
   * @param user id of candidate user.
   * @return builder instance.
   */
  fun addCandidateUser(user: String) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.AddCandidateUserTaskCmd(taskId, user)
  }

  /**
   * Adds candidate group.
   * @param group id of the group.
   * @return builder instance.
   */
  fun addCandidateGroup(group: String) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.AddCandidateGroupTaskCmd(taskId, group)
  }

  /**
   * Removes candidate user.
   * @param user of candidate user.
   * @return builder instance.
   */
  fun removeCandidateUser(user: String) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.RemoveCandidateUserTaskCmd(taskId, user)
  }

  /**
   * Removes candidate group.
   * @param group of candidate group.
   * @return builder instance.
   */
  fun removeCandidateGroup(group: String) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.RemoveCandidateGroupTaskCmd(taskId, group)
  }

  /**
   * Sets candidate users replacing existing.
   * @param users list of user ids.
   * @return builder instance.
   */
  fun setCandidateUsers(users: List<String>) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.SetCandidateUsersTaskCmd(taskId, users)
  }

  /**
   * Sets candidate groups replacing existing.
   * @param groups list of group ids.
   * @return builder instance.
   */
  fun setCandidateGroups(groups: List<String>) = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.SetCandidateGroupsTaskCmd(taskId, groups)
  }

  /**
   * Clears candidate users.
   * @return builder instance.
   */
  fun clearCandidateUsers() = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.ClearCandidateUsersTaskCmd(taskId)
  }

  /**
   * Clears candidate groups.
   * @return builder instance.
   */
  fun clearCandidateGroups() = this.apply {
    commands += ChangeAssignmentModifyTaskCmd.ClearCandidateGroupsTaskCmd(taskId)
  }

  /**
   * Builds the composite command.
   * @return composite modification command.
   */
  fun build(): CompositeModifyTaskCmd =
    CompositeModifyTaskCmd(taskId, commands)
}
