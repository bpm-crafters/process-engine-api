package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class VariablesExtKtTest {


  @Test
  fun `should filter variables`() {
    val vars = mapOf("foo" to "1", "bar" to "2", "baz" to "3")
    val handle = handle(setOf("foo", "bar"))
    assertThat(vars.filterBySubscription(handle)).containsOnlyKeys("foo", "bar")
  }

  @Test
  fun `should filter all variables with empty restrictions`() {
    val vars = mapOf("foo" to "1", "bar" to "2", "baz" to "3")
    val handle = handle(setOf())
    assertThat(vars.filterBySubscription(handle)).isEmpty()
  }

  @Test
  fun `should not filter variables with null restrictions`() {
    val vars = mapOf("foo" to "1", "bar" to "2", "baz" to "3")
    val handle = handle(null)
    assertThat(vars.filterBySubscription(handle)).containsOnlyKeys("foo", "bar", "baz")
  }


  fun handle(description: Set<String>?) = TaskSubscriptionHandle(
    taskType = TaskType.EXTERNAL,
    payloadDescription = description,
    restrictions = CommonRestrictions.builder().build(),
    taskDescriptionKey = null,
    { _, _ -> },
    { _ -> }
  )
}
