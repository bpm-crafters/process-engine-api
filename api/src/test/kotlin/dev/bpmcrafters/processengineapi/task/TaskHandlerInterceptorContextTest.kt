package dev.bpmcrafters.processengineapi.task

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TaskHandlerInterceptorContextTest {

  @Test
  fun `exposes the intercepted execution details`() {
    val taskInformation = TaskInformation("task-id", mapOf("meta-key" to "meta-value"))
    val payload = mapOf("var1" to "value1")

    val context = TaskHandlerInterceptorContext(taskInformation, payload, "task-key", TaskType.EXTERNAL)

    assertThat(context.taskInformation).isEqualTo(taskInformation)
    assertThat(context.payload).isEqualTo(payload)
    assertThat(context.taskDescriptionKey).isEqualTo("task-key")
    assertThat(context.taskType).isEqualTo(TaskType.EXTERNAL)
  }
}
