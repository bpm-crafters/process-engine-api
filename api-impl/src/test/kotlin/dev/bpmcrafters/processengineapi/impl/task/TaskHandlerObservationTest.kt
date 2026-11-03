package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandlerContext
import dev.bpmcrafters.processengineapi.task.TaskHandlerObserver
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class TaskHandlerObservationTest {

  private val taskInformation = TaskInformation("task-id", mutableMapOf("meta-key" to "meta-value"))
  private val payload = mutableMapOf<String, Any?>("var1" to "value1")
  private val trace = mutableListOf<String>()

  private inner class TracingObserver(private val name: String) : TaskHandlerObserver<String> {
    var seenOutcome: TaskHandlerOutcome? = null
    var seenError: Throwable? = null

    override fun before(context: TaskHandlerContext): String {
      trace += "$name-before"
      return "$name-state"
    }

    override fun after(context: TaskHandlerContext, state: String, outcome: TaskHandlerOutcome, error: Throwable?) {
      trace += "$name-after:$state"
      seenOutcome = outcome
      seenError = error
    }
  }

  private fun throwingBefore(failure: Throwable) = object : TaskHandlerObserver<Unit> {
    override fun before(context: TaskHandlerContext) = throw failure
    override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) {
      trace += "failing-after"
    }
  }

  private fun throwingAfter(failure: Throwable) = object : TaskHandlerObserver<Unit> {
    override fun before(context: TaskHandlerContext) = Unit
    override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) = throw failure
  }

  private fun subscription(taskType: TaskType = TaskType.EXTERNAL) =
    TaskSubscriptionHandle(taskType, null, emptyMap(), "task-key", { _, _ -> }, { })

  private fun observe(
    vararg observers: TaskHandlerObserver<*>,
    taskType: TaskType = TaskType.EXTERNAL,
    delivery: (TaskDeliveryReport) -> Unit = { trace += "delivery" }
  ) = TaskHandlerObservation(observers.toList()).observe(subscription(taskType), taskInformation, payload, delivery)

  @Test
  fun `runs observers around delivery and hands each its own state`() {
    observe(TracingObserver("first"), TracingObserver("second"))

    assertThat(trace).containsExactly(
      "first-before", "second-before", "delivery", "second-after:second-state", "first-after:first-state"
    )
  }

  @Test
  fun `runs delivery failure handling between before and after`() {
    val observer = TracingObserver("first")
    val handlerError = IllegalStateException("boom")

    observe(observer) { report ->
      try {
        throw handlerError
      } catch (e: Exception) {
        trace += "failure-handling"
        report.failed(e)
      }
    }

    assertThat(trace).containsExactly("first-before", "failure-handling", "first-after:first-state")
    assertThat(observer.seenOutcome).isEqualTo(TaskHandlerOutcome.Failed("boom", null))
    assertThat(observer.seenError).isSameAs(handlerError)
  }

  @Test
  fun `reports vanished task and lets it take precedence over failed`() {
    val observer = TracingObserver("first")
    val notFound = IllegalStateException("not found")

    observe(observer) { report ->
      report.vanished(notFound)
      report.failed(notFound)
    }

    assertThat(observer.seenOutcome).isEqualTo(TaskHandlerOutcome.Vanished)
    assertThat(observer.seenError).isSameAs(notFound)
  }

  @Test
  fun `lets reported failure take precedence over outcome recorded by the handler`() {
    val observer = TracingObserver("first")

    observe(observer) { report ->
      TaskHandlerOutcomeRecorder.record("task-id", TaskHandlerOutcome.Completed)
      report.failed(IllegalStateException("not found"))
    }

    assertThat(observer.seenOutcome).isEqualTo(TaskHandlerOutcome.Failed("not found", null))
  }

  @Test
  fun `passes outcome recorded by the handler if delivery reports nothing`() {
    val observer = TracingObserver("first")

    observe(observer) { TaskHandlerOutcomeRecorder.record("task-id", TaskHandlerOutcome.Completed) }

    assertThat(observer.seenOutcome).isEqualTo(TaskHandlerOutcome.Completed)
    assertThat(observer.seenError).isNull()
  }

  @Test
  fun `propagates exception thrown by delivery unchanged and passes it to observers`() {
    val observer = TracingObserver("first")
    val deliveryError = IllegalStateException("delivery")

    assertThatThrownBy { observe(observer, throwingAfter(IllegalArgumentException("observer"))) { throw deliveryError } }
      .isSameAs(deliveryError)
    assertThat(observer.seenError).isSameAs(deliveryError)
  }

  @Test
  fun `propagates error thrown by delivery unchanged`() {
    val deliveryError = AssertionError("delivery")

    assertThatThrownBy { observe(TracingObserver("first")) { throw deliveryError } }.isSameAs(deliveryError)
    assertThat(trace).containsExactly("first-before", "first-after:first-state")
  }

  @Test
  fun `runs delivery and other observers if before throws and skips after of the failed observer`() {
    observe(TracingObserver("first"), throwingBefore(IllegalStateException("boom")))

    assertThat(trace).containsExactly("first-before", "delivery", "first-after:first-state")
  }

  @Test
  fun `does not propagate exception thrown by after and still runs other observers`() {
    observe(TracingObserver("first"), throwingAfter(IllegalStateException("boom")))

    assertThat(trace).containsExactly("first-before", "delivery", "first-after:first-state")
  }

  @Test
  fun `only logs errors thrown by observers`() {
    val observer = TracingObserver("first")

    observe(observer, throwingBefore(AssertionError("before")), throwingAfter(NoClassDefFoundError("after")))

    assertThat(trace).containsExactly("first-before", "delivery", "first-after:first-state")
    assertThat(observer.seenError).isNull()
  }

  @Test
  fun `propagates virtual machine errors thrown by observers`() {
    assertThatThrownBy { observe(throwingBefore(StackOverflowError("before"))) }
      .isInstanceOf(StackOverflowError::class.java)
  }

  @Test
  fun `runs delivery without observers for other task types`() {
    observe(TracingObserver("first"), taskType = TaskType.USER)

    assertThat(trace).containsExactly("delivery")
  }

  @Test
  fun `runs delivery without observers`() {
    observe()

    assertThat(trace).containsExactly("delivery")
  }

  @Test
  fun `invokes delivery exactly once`() {
    var invocations = 0

    observe(TracingObserver("first")) { invocations++ }

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

    observe(capturing)

    val context = captured!!
    assertThat(context.taskInformation).isEqualTo(taskInformation)
    assertThat(context.payload).isEqualTo(payload)
    assertThat(context.taskDescriptionKey).isEqualTo("task-key")
    assertThat(context.taskType).isEqualTo(TaskType.EXTERNAL)
    assertThatThrownBy { (context.payload as MutableMap<String, Any?>)["key"] = "value" }
      .isInstanceOf(UnsupportedOperationException::class.java)
    assertThatThrownBy { (context.taskInformation.meta as MutableMap<String, String>)["key"] = "value" }
      .isInstanceOf(UnsupportedOperationException::class.java)
  }

  @Test
  fun `describes context without payload`() {
    val context = TaskHandlerContext(taskInformation, mapOf("secret" to "value"), "task-key", TaskType.EXTERNAL)

    assertThat(context.toString())
      .isEqualTo("TaskHandlerContext(taskId=task-id, taskDescriptionKey=task-key, taskType=EXTERNAL)")
  }

}
