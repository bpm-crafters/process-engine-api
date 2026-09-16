package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptorContext
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class InterceptingTaskHandlerTest {

  private val taskInformation = TaskInformation("task-id", mapOf("meta-key" to "meta-value"))
  private val payload = mapOf("var1" to "value1")

  @Test
  fun `runs interceptors around delegate in list order`() {
    val trace = mutableListOf<String>()
    val first = TaskHandlerInterceptor { _, chain ->
      trace += "first-before"
      chain.proceed()
      trace += "first-after"
    }
    val second = TaskHandlerInterceptor { _, chain ->
      trace += "second-before"
      chain.proceed()
      trace += "second-after"
    }
    val delegate = TaskHandler { _, _ -> trace += "delegate" }

    InterceptingTaskHandler(delegate, listOf(first, second), "task-key", TaskType.EXTERNAL)
      .accept(taskInformation, payload)

    assertThat(trace).containsExactly(
      "first-before", "second-before", "delegate", "second-after", "first-after"
    )
  }

  @Test
  fun `provides context to interceptors`() {
    var captured: TaskHandlerInterceptorContext? = null
    val interceptor = TaskHandlerInterceptor { context, chain ->
      captured = context
      chain.proceed()
    }

    InterceptingTaskHandler(TaskHandler { _, _ -> }, listOf(interceptor), "task-key", TaskType.USER)
      .accept(taskInformation, payload)

    assertThat(captured).isEqualTo(
      TaskHandlerInterceptorContext(taskInformation, payload, "task-key", TaskType.USER)
    )
  }

  @Test
  fun `interceptor without proceed prevents delegate invocation`() {
    var delegateCalled = false
    val interceptor = TaskHandlerInterceptor { _, _ -> }
    val delegate = TaskHandler { _, _ -> delegateCalled = true }

    InterceptingTaskHandler(delegate, listOf(interceptor), "task-key", TaskType.EXTERNAL)
      .accept(taskInformation, payload)

    assertThat(delegateCalled).isFalse()
  }

  @Test
  fun `delegate exception propagates through the chain`() {
    val interceptor = TaskHandlerInterceptor { _, chain -> chain.proceed() }
    val delegate = TaskHandler { _, _ -> throw IllegalStateException("boom") }

    assertThatThrownBy {
      InterceptingTaskHandler(delegate, listOf(interceptor), "task-key", TaskType.EXTERNAL)
        .accept(taskInformation, payload)
    }.isInstanceOf(IllegalStateException::class.java).hasMessage("boom")
  }
}
