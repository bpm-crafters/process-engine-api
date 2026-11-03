package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandlerContext
import dev.bpmcrafters.processengineapi.task.TaskHandlerObserver
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.net.URLClassLoader

internal class ObserverAwareTaskHandlerTest {

  private val taskInformation = TaskInformation("task-id", mutableMapOf("meta-key" to "meta-value"))
  private val payload = mutableMapOf<String, Any?>("var1" to "value1")
  private val trace = mutableListOf<String>()

  private inner class TracingObserver(private val name: String) : TaskHandlerObserver<String> {
    override fun before(context: TaskHandlerContext): String {
      trace += "$name-before"
      return "$name-state"
    }

    override fun after(context: TaskHandlerContext, state: String, outcome: TaskHandlerOutcome, error: Throwable?) {
      trace += "$name-after:$state"
    }
  }

  private fun observed(delegate: TaskHandler, vararg observers: TaskHandlerObserver<*>) =
    ObserverAwareTaskHandler(delegate, observers.toList(), "task-key", TaskType.EXTERNAL)

  @Test
  fun `runs observers around handler and hands each its own state`() {
    observed({ _, _ -> trace += "handler" }, TracingObserver("first"), TracingObserver("second"))
      .accept(taskInformation, payload)

    assertThat(trace).containsExactly(
      "first-before", "second-before", "handler", "second-after:second-state", "first-after:first-state"
    )
  }

  @Test
  fun `runs handler and other observers if before throws and skips after of the failed observer`() {
    val failing = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = throw IllegalStateException("boom")
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) {
        trace += "failing-after"
      }
    }

    observed({ _, _ -> trace += "handler" }, TracingObserver("first"), failing)
      .accept(taskInformation, payload)

    assertThat(trace).containsExactly("first-before", "handler", "first-after:first-state")
  }

  @Test
  fun `does not propagate exception thrown by after and still runs other observers`() {
    val failing = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = Unit
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) =
        throw IllegalStateException("boom")
    }

    observed({ _, _ -> trace += "handler" }, TracingObserver("first"), failing)
      .accept(taskInformation, payload)

    assertThat(trace).containsExactly("first-before", "handler", "first-after:first-state")
  }

  @Test
  fun `propagates handler exception unchanged and passes it to observers`() {
    var seenError: Throwable? = null
    val observing = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = Unit
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) {
        seenError = error
        throw IllegalArgumentException("observer")
      }
    }
    val handlerError = IllegalStateException("handler")

    assertThatThrownBy { observed({ _, _ -> throw handlerError }, observing).accept(taskInformation, payload) }
      .isSameAs(handlerError)
    assertThat(seenError).isSameAs(handlerError)
  }

  @Test
  fun `only logs errors thrown by observers and still runs handler and other observers`() {
    val failingBefore = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = throw AssertionError("before")
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) = Unit
    }
    val failingAfter = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = Unit
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) =
        throw NoClassDefFoundError("after")
    }
    var seenError: Throwable? = Throwable("not called")
    val observing = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = Unit
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) {
        seenError = error
      }
    }

    observed({ _, _ -> trace += "handler" }, observing, failingBefore, failingAfter).accept(taskInformation, payload)

    assertThat(trace).containsExactly("handler")
    assertThat(seenError).isNull()
  }

  @Test
  fun `propagates virtual machine errors thrown by observers`() {
    val failing = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) = throw StackOverflowError("before")
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) = Unit
    }

    assertThatThrownBy { observed({ _, _ -> }, failing).accept(taskInformation, payload) }
      .isInstanceOf(StackOverflowError::class.java)
  }

  @Test
  fun `propagates error thrown by handler unchanged`() {
    val handlerError = AssertionError("handler")

    assertThatThrownBy { observed({ _, _ -> throw handlerError }, TracingObserver("first")).accept(taskInformation, payload) }
      .isSameAs(handlerError)
    assertThat(trace).containsExactly("first-before", "first-after:first-state")
  }

  @Test
  fun `describes context without payload`() {
    val context = TaskHandlerContext(taskInformation, mapOf("secret" to "value"), "task-key", TaskType.EXTERNAL)

    assertThat(context.toString())
      .isEqualTo("TaskHandlerContext(taskId=task-id, taskDescriptionKey=task-key, taskType=EXTERNAL)")
  }

  @Test
  fun `invokes handler exactly once`() {
    var invocations = 0

    observed({ _, _ -> invocations++ }, TracingObserver("first")).accept(taskInformation, payload)

    assertThat(invocations).isEqualTo(1)
  }

  @Test
  fun `provides read-only context`() {
    var captured: TaskHandlerContext? = null
    val capturing = object : TaskHandlerObserver<Unit> {
      override fun before(context: TaskHandlerContext) {
        captured = context
      }
      override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) = Unit
    }

    ObserverAwareTaskHandler({ _, _ -> }, listOf(capturing), "task-key", TaskType.USER).accept(taskInformation, payload)

    val context = captured!!
    assertThat(context.taskInformation).isEqualTo(taskInformation)
    assertThat(context.payload).isEqualTo(payload)
    assertThat(context.taskDescriptionKey).isEqualTo("task-key")
    assertThat(context.taskType).isEqualTo(TaskType.USER)
    assertThatThrownBy { (context.payload as MutableMap<String, Any?>)["key"] = "value" }
      .isInstanceOf(UnsupportedOperationException::class.java)
    assertThatThrownBy { (context.taskInformation.meta as MutableMap<String, String>)["key"] = "value" }
      .isInstanceOf(UnsupportedOperationException::class.java)
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
      val observing = object : TaskHandlerObserver<Unit> {
        override fun before(context: TaskHandlerContext) {
          seenClassLoader = Thread.currentThread().contextClassLoader
        }
        override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) = Unit
      }

      observed(action, observing).accept(taskInformation, payload)

      assertThat(seenClassLoader).isSameAs(action.javaClass.classLoader)
      assertThat(thread.contextClassLoader).isSameAs(foreignClassLoader)
    } finally {
      thread.contextClassLoader = originalClassLoader
    }
  }
}
