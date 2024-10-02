package dev.bpmcrafters.processengineapi.adapter.commons.task

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
    val payload = mapOf("key" to "value")

    support.addHandler { ti, _ -> delivered = ti.taskId }
    support.addTerminationHandler { id -> removed = id }

    val taskId = UUID.randomUUID().toString()

    assertThat(support.exists(taskId)).isFalse()
    assertThat(delivered).isNull()
    assertThat(removed).isNull()

    support.onTaskDelivery(TaskInformation(taskId, mapOf(CommonRestrictions.TASK_DEFINITION_KEY to "some")), payload)

    assertThat(delivered).isEqualTo(taskId)
    assertThat(removed).isNull()
    assertThat(support.exists(taskId)).isTrue()
    assertThat(support.exists(taskId, "some")).isTrue()
    assertThat(support.getPayload(taskId)).containsAllEntriesOf(payload)


    support.onTaskRemoval(taskId)
    assertThat(delivered).isEqualTo(taskId)
    assertThat(removed).isEqualTo(taskId)
    assertThat(support.exists(taskId)).isFalse()
  }
}
