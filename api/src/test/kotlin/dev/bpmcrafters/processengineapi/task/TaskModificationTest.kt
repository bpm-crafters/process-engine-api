package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.function.Supplier

internal class TaskModificationTest {


  @Test
  fun `should create composite`() {
    val composite = CompositeModifyTaskCmd(
      taskId = "taskId", commands = listOf(
        ChangeAssignmentModifyTaskCmd.AssignTaskCmd(taskId = "taskId", assignee = "kermit"),
        ChangeAssignmentModifyTaskCmd.AddCandidateUserTaskCmd(taskId = "taskId", candidateUser = "kermit"),
      )
    )
    assertThat(composite).isNotNull
    assertThat(composite.commands).hasSize(2)
    assertThat(composite.commands.map { it.taskId }.distinct()).containsExactly("taskId")
    assertThat(composite.taskId).isEqualTo("taskId")
  }


  @Test
  fun `should fail if different task ids are combined`() {
    assertThatThrownBy {
      CompositeModifyTaskCmd(
        taskId = "taskId", commands = listOf(
          ChangeAssignmentModifyTaskCmd.AssignTaskCmd(taskId = "taskId", assignee = "kermit"),
          ChangeAssignmentModifyTaskCmd.AddCandidateUserTaskCmd(taskId = "otherTaskId", candidateUser = "kermit"),
        )
      )
    }.isInstanceOf(IllegalArgumentException::class.java).hasMessage("All nested commands must have the same taskId.")
  }

  @Test
  fun `creates composition using receiver function`() {
    val dueDate = OffsetDateTime.parse("2026-04-02T12:00:00+02:00")
    val followUpDate = OffsetDateTime.parse("2026-04-01T12:00:00+02:00")
    val composite = TaskModification.taskModification("taskId") {
      unassign()
      assign("kermit")
      setCandidateUsers(listOf("piggy"))
      addCandidateUser("kermit")
      removeCandidateUser("piggy")
      setCandidateGroups(listOf("avengers"))
      addCandidateGroup("muppets")
      removeCandidateGroup("avengers")
      clearCandidateGroups()
      clearCandidateUsers()
      updatePayload {
        mapOf("task-modified" to true)
      }
      updatePayload(
        mapOf("task-modified" to false)
      )
      clearPayload()
      deletePayload(listOf("some-var"))
      deletePayload {
        listOf("some-var-other")
      }
      setDueDate(dueDate)
      clearDueDate()
      setFollowUpDate(followUpDate)
      clearFollowUpDate()
    }

    assertThat(composite).isNotNull
    assertThat(composite.commands).hasSize(19)
    assertThat(composite.taskId).isEqualTo("taskId")
    assertThat(composite.commands.map { it.taskId }.distinct()).containsExactly("taskId")
    assertThat(composite.commands).anyMatch { it is ChangeDatesModifyTaskCmd.SetDueDateTaskCmd && it.dueDate == dueDate }
    assertThat(composite.commands).anyMatch { it is ChangeDatesModifyTaskCmd.SetFollowUpDateTaskCmd && it.followUpDate == followUpDate }

  }

  @Test
  fun `create composition using builder`() {
    val supplier = PayloadSupplier { mapOf("task-modified" to true) }
    val keySupplier = Supplier { listOf("task-modified") }
    val dueDate = OffsetDateTime.parse("2026-04-02T12:00:00+02:00")
    val followUpDate = OffsetDateTime.parse("2026-04-01T12:00:00+02:00")
    val composite = TaskModification("taskId")
      .assign("kermit")
      .unassign()
      .clearCandidateUsers()
      .clearCandidateGroups()
      .addCandidateGroup("avengers")
      .removeCandidateGroup("muppets")
      .addCandidateUser("kermit")
      .removeCandidateUser("piggy")
      .setCandidateUsers(listOf("fozzy"))
      .setCandidateGroups(listOf("avengers"))
      .updatePayload(mapOf("task-modified" to true))
      .updatePayload(supplier)
      .deletePayload(listOf("task-modified"))
      .deletePayload(keySupplier)
      .clearPayload()
      .setDueDate(dueDate)
      .clearDueDate()
      .setFollowUpDate(followUpDate)
      .clearFollowUpDate()
      .build()
    assertThat(composite).isNotNull
    assertThat(composite.commands).hasSize(19)
    assertThat(composite.commands).anyMatch { it is ChangeDatesModifyTaskCmd.SetDueDateTaskCmd && it.dueDate == dueDate }
    assertThat(composite.commands).anyMatch { it is ChangeDatesModifyTaskCmd.SetFollowUpDateTaskCmd && it.followUpDate == followUpDate }
  }
}
