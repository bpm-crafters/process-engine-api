package dev.bpmcrafters.processengineapi.task.support

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*


internal class UserTaskSupportTest {

  private val support = UserTaskSupport(assignmentDetector = object: AssignmentDetector {
    override fun hasChangedAssignment(oldTaskInformation: Pair<TaskInformation, Map<String, Any?>>, newTaskInformation: Pair<TaskInformation, Map<String, Any?>>): Boolean {
      return oldTaskInformation.first.meta["custom-assignment-field"] != newTaskInformation.first.meta["custom-assignment-field"]
    }
  })

  @Test
  fun `acts as composite task handler`() {
    var delivered: TaskInformation? = null
    var removed: TaskInformation? = null

    support.addHandler { ti, _ -> delivered = ti }
    support.addTerminationHandler { ti -> removed = ti }

    val taskId = UUID.randomUUID().toString()
    val task = TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some"))
    val payload = mapOf("key" to "value")

    // given
    assertThat(support.getAllTasks()).isEmpty()
    assertThat(support.exists(taskId)).isFalse()
    assertThat(delivered).isNull()
    assertThat(removed).isNull()

    // when
    support.onTaskDelivery(task, payload)

    // then
    assertThat(support.exists(taskId)).isTrue()
    assertThat(support.exists(taskId, "some")).isTrue()
    assertThat(support.getAllTasks()).containsExactly(task)
    assertThat(support.getPayload(taskId)).containsAllEntriesOf(payload)
    assertThat(delivered).isEqualTo(task)
    assertThat(removed).isNull()

    // when
    support.onTaskRemoval(task)

    // then
    assertThat(support.getAllTasks()).isEmpty()
    assertThat(support.exists(taskId)).isFalse()
    assertThat(delivered).isEqualTo(task)
    assertThat(removed).isEqualTo(task)
  }

  @Test
  fun `handles reason propagation correctly`() {
    var reason: String? = null
    var terminationReason: String? = null
    support.addHandler { ti, _ -> reason = ti.meta[TaskInformation.REASON] }
    support.addTerminationHandler { ti -> terminationReason = ti.meta[TaskInformation.REASON] }

    val taskId = UUID.randomUUID().toString()
    val payload = mapOf("key" to "value")

    // create
    support.onTaskDelivery(
      TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some")).withReason(TaskInformation.CREATE),
      payload
    )
    assertThat(reason).isEqualTo(TaskInformation.CREATE)
    assertThat(terminationReason).isNull()
    assertThat(support.exists(taskId)).isTrue()
    assertThat(support.getTaskInformation(taskId).meta).doesNotContainKeys(TaskInformation.REASON)

    // update
    support.onTaskDelivery(
      TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some")).withReason(TaskInformation.UPDATE),
      payload + ("other key" to "other value")
    )
    assertThat(reason).isEqualTo(TaskInformation.UPDATE)
    assertThat(terminationReason).isNull()
    assertThat(support.exists(taskId)).isTrue()
    assertThat(support.getTaskInformation(taskId).meta).doesNotContainKeys(TaskInformation.REASON)

    // assign
    support.onTaskDelivery(
      TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some", "custom-assignment-field" to "kermit")).withReason(TaskInformation.UPDATE),
      payload + ("other key" to "other value")
    )
    assertThat(reason).isEqualTo(TaskInformation.ASSIGN)
    assertThat(terminationReason).isNull()
    assertThat(support.exists(taskId)).isTrue()
    assertThat(support.getTaskInformation(taskId).meta).doesNotContainKeys(TaskInformation.REASON)

    // complete
    reason = "foo"
    support.onTaskRemoval(TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some")).withReason(TaskInformation.COMPLETE))
    assertThat(reason).isEqualTo("foo")
    assertThat(terminationReason).isEqualTo(TaskInformation.COMPLETE)
    assertThat(support.exists(taskId)).isFalse()

    // create again
    support.onTaskDelivery(
      TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some")).withReason(TaskInformation.CREATE),
      payload
    )
    assertThat(support.exists(taskId)).isTrue()

    // delete
    reason = "bar"
    support.onTaskRemoval(TaskInformation(taskId, mapOf(CommonRestrictions.ACTIVITY_ID to "some")).withReason(TaskInformation.DELETE))
    assertThat(reason).isEqualTo("bar")
    assertThat(terminationReason).isEqualTo(TaskInformation.DELETE)
    assertThat(support.exists(taskId)).isFalse()

  }


}
