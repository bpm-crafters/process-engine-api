package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionDecorator
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class TaskExecutionDecorationTest {

  private val task = TaskInformation("task-id", mutableMapOf("topicName" to "my-topic"))
  private val payload = mutableMapOf<String, Any?>("var1" to "value1")
  private val trace = mutableListOf<String>()
  private val execution = Runnable { trace += "execution" }

  private fun tracing(name: String) = TaskExecutionDecorator { _, _, inner ->
    Runnable {
      trace += "$name-before"
      try {
        inner.run()
      } finally {
        trace += "$name-after"
      }
    }
  }

  @Test
  fun `runs execution directly without decorators`() {
    TaskExecutionDecoration.NONE.run(task, payload, execution)

    assertThat(trace).containsExactly("execution")
  }

  @Test
  fun `is not affected by later changes of the decorator list`() {
    val decorators = mutableListOf(tracing("first"))
    val decoration = TaskExecutionDecoration(decorators)
    decorators += tracing("second")

    decoration.run(task, payload, execution)

    assertThat(trace).containsExactly("first-before", "execution", "first-after")
  }

  @Test
  fun `runs decorators around execution with the first one outermost`() {
    TaskExecutionDecoration(listOf(tracing("first"), tracing("second"))).run(task, payload, execution)

    assertThat(trace).containsExactly("first-before", "second-before", "execution", "second-after", "first-after")
  }

  @Test
  fun `passes read-only task and payload to decorators`() {
    var seenTask: TaskInformation? = null
    var seenPayload: Map<String, Any?>? = null
    val capturing = TaskExecutionDecorator { decoratedTask, decoratedPayload, inner ->
      seenTask = decoratedTask
      seenPayload = decoratedPayload
      inner
    }

    TaskExecutionDecoration(listOf(capturing)).run(task, payload, execution)

    assertThat(seenTask).isEqualTo(task)
    assertThat(seenPayload).isEqualTo(payload)
    assertThatThrownBy { (seenTask!!.meta as MutableMap<String, String>)["key"] = "value" }
      .isInstanceOf(UnsupportedOperationException::class.java)
    assertThatThrownBy { (seenPayload as MutableMap<String, Any?>)["key"] = "value" }
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun `runs execution once if a decorator skips it`() {
    val skipping = TaskExecutionDecorator { _, _, _ -> Runnable { trace += "skipping" } }

    TaskExecutionDecoration(listOf(tracing("first"), skipping)).run(task, payload, execution)

    assertThat(trace).containsExactly("first-before", "skipping", "first-after", "execution")
  }

  @Test
  fun `runs execution once if a decorator repeats it`() {
    val repeating = TaskExecutionDecorator { _, _, inner -> Runnable { inner.run(); inner.run() } }

    TaskExecutionDecoration(listOf(repeating)).run(task, payload, execution)

    assertThat(trace).containsExactly("execution")
  }

  @Test
  fun `runs execution with remaining decorators if decorate throws`() {
    val failing = TaskExecutionDecorator { _, _, _ -> throw IllegalStateException("boom") }

    TaskExecutionDecoration(listOf(tracing("first"), failing)).run(task, payload, execution)

    assertThat(trace).containsExactly("first-before", "execution", "first-after")
  }

  @Test
  fun `runs execution if a decorator throws before running it`() {
    val failing = TaskExecutionDecorator { _, _, _ -> Runnable { throw AssertionError("boom") } }

    TaskExecutionDecoration(listOf(failing)).run(task, payload, execution)

    assertThat(trace).containsExactly("execution")
  }

  @Test
  fun `only logs exception thrown by a decorator after the execution ran`() {
    val failing = TaskExecutionDecorator { _, _, inner -> Runnable { inner.run(); throw IllegalStateException("boom") } }

    TaskExecutionDecoration(listOf(failing)).run(task, payload, execution)

    assertThat(trace).containsExactly("execution")
  }

  @Test
  fun `propagates exception thrown by the execution unchanged through decorators`() {
    val executionError = IllegalStateException("execution")

    assertThatThrownBy { TaskExecutionDecoration(listOf(tracing("first"))).run(task, payload) { throw executionError } }
      .isSameAs(executionError)
    assertThat(trace).containsExactly("first-before", "first-after")
  }

  @Test
  fun `propagates error thrown by the execution unchanged through decorators`() {
    val executionError = AssertionError("execution")

    assertThatThrownBy { TaskExecutionDecoration(listOf(tracing("first"))).run(task, payload) { throw executionError } }
      .isSameAs(executionError)
  }

  @Test
  fun `propagates virtual machine errors thrown by decorators`() {
    val failing = TaskExecutionDecorator { _, _, _ -> throw StackOverflowError("boom") }

    assertThatThrownBy { TaskExecutionDecoration(listOf(failing)).run(task, payload, execution) }
      .isInstanceOf(StackOverflowError::class.java)
  }
}
