package dev.bpmcrafters.processengineapi.task.support

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*


internal class UserTaskSupportTest {

  private val support = UserTaskSupport()

  @Test
  fun `acts as composite task handler`() {
    var delivered: String? = null
    var removed: String? = null

    support.addHandler { ti, _ -> delivered = ti.taskId }
    support.addTerminationHandler { id -> removed = id }

    val taskId = UUID.randomUUID().toString()
    val task = TaskInformation(taskId, mapOf(CommonRestrictions.TASK_DEFINITION_KEY to "some"))
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
    assertThat(delivered).isEqualTo(taskId)
    assertThat(removed).isNull()

    // when
    support.onTaskRemoval(taskId)

    // then
    assertThat(support.getAllTasks()).isEmpty()
    assertThat(support.exists(taskId)).isFalse()
    assertThat(delivered).isEqualTo(taskId)
    assertThat(removed).isEqualTo(taskId)
  }
}
