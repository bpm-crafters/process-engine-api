package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptorContext
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.net.URLClassLoader

internal class InterceptingTaskHandlerTest {

  private val taskInformation = TaskInformation("task-id", mapOf("meta-key" to "meta-value"))
  private val payload = mapOf("var1" to "value1")

  @Test
  fun `propagates exception thrown by interceptor without invoking handler`() {
    var handlerInvoked = false
    val interceptor = TaskHandlerInterceptor { _, _ -> throw IllegalStateException("boom") }
    val handler = InterceptingTaskHandler({ _, _ -> handlerInvoked = true }, listOf(interceptor), "key", TaskType.EXTERNAL)

    assertThatThrownBy { handler.accept(TaskInformation("task-id", emptyMap()), emptyMap()) }
      .isInstanceOf(IllegalStateException::class.java)

    assertThat(handlerInvoked).isFalse()
  }

  @Test
  fun `does not propagate exception thrown by interceptor after handler returned`() {
    val interceptor = TaskHandlerInterceptor { _, chain ->
      chain.proceed()
      throw IllegalStateException("boom")
    }

    InterceptingTaskHandler({ _, _ -> }, listOf(interceptor), "key", TaskType.EXTERNAL)
      .accept(TaskInformation("task-id", emptyMap()), emptyMap())
  }

  @Test
  fun `propagates handler exception of a second attempt`() {
    var attempts = 0
    val interceptor = TaskHandlerInterceptor { _, chain ->
      chain.proceed()
      chain.proceed()
    }
    val delegate = TaskHandler { _, _ -> if (attempts++ == 1) throw IllegalStateException("boom") }

    assertThatThrownBy {
      InterceptingTaskHandler(delegate, listOf(interceptor), "task-key", TaskType.EXTERNAL)
        .accept(taskInformation, payload)
    }.isInstanceOf(IllegalStateException::class.java).hasMessage("boom")
  }

  @Test
  fun `exposes payload as unmodifiable view`() {
    val interceptor = TaskHandlerInterceptor { context, chain ->
      assertThatThrownBy { (context.payload as MutableMap<String, Any?>)["key"] = "value" }
        .isInstanceOf(UnsupportedOperationException::class.java)
      chain.proceed()
    }

    InterceptingTaskHandler({ _, _ -> }, listOf(interceptor), "key", TaskType.EXTERNAL)
      .accept(TaskInformation("task-id", emptyMap()), mutableMapOf())
  }

  @Test
  fun `runs with class loader of handler and restores the original one`() {
    val thread = Thread.currentThread()
    val originalClassLoader = thread.contextClassLoader
    val foreignClassLoader = URLClassLoader(emptyArray())
    thread.contextClassLoader = foreignClassLoader
    try {
      val action = TaskHandler { _, _ -> }
      var seenClassLoader: ClassLoader? = null
      val interceptor = TaskHandlerInterceptor { _, chain ->
        seenClassLoader = Thread.currentThread().contextClassLoader
        chain.proceed()
      }

      InterceptingTaskHandler(action, listOf(interceptor), "key", TaskType.EXTERNAL)
        .accept(TaskInformation("task-id", emptyMap()), emptyMap())

      assertThat(seenClassLoader).isSameAs(action.javaClass.classLoader)
      assertThat(thread.contextClassLoader).isSameAs(foreignClassLoader)
    } finally {
      thread.contextClassLoader = originalClassLoader
    }
  }

  @Test
  fun `runs interceptors around delegate in list order`() {
    val trace = mutableListOf<String>()
    val first = TaskHandlerInterceptor { _, chain ->
      trace += "first-before"
      chain.proceed().also { trace += "first-after" }
    }
    val second = TaskHandlerInterceptor { _, chain ->
      trace += "second-before"
      chain.proceed().also { trace += "second-after" }
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
