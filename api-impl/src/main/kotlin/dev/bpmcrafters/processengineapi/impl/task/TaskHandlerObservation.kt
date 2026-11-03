package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandlerContext
import dev.bpmcrafters.processengineapi.task.TaskHandlerObserver
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections

private val logger = KotlinLogging.logger {}

/**
 * Runs [TaskHandlerObserver]s around the delivery of one external task. A delivery calls [observe]
 * around its whole per-task work, so the handler call and the delivery's own failure handling run
 * between [TaskHandlerObserver.before] and [TaskHandlerObserver.after]: logs written while reporting
 * a failure still see the context an observer opened.
 *
 * Observers run in list order before and in reverse order after the delivery. Everything an
 * observer throws is only logged, except a [VirtualMachineError], so the delivery behaves the same
 * as without observers. Only `EXTERNAL` tasks are observed; for other task types and without
 * observers, the delivery just runs.
 * @since 1.8
 */
class TaskHandlerObservation(
  private val observers: List<TaskHandlerObserver<*>>
) {

  /**
   * Observes the delivery of a task to [subscription].
   * @param subscription subscription the task is delivered to.
   * @param taskInformation task information passed to the handler.
   * @param payload payload passed to the handler.
   * @param delivery per-task work of the delivery, including its failure handling. It reports what
   * the handler could not report itself through the given [TaskDeliveryReport].
   */
  fun observe(
    subscription: TaskSubscriptionHandle,
    taskInformation: TaskInformation,
    payload: Map<String, Any?>,
    delivery: (TaskDeliveryReport) -> Unit
  ) {
    if (observers.isEmpty() || subscription.taskType != TaskType.EXTERNAL) {
      delivery(TaskDeliveryReport.IGNORED)
      return
    }
    val context = TaskHandlerContext(
      taskInformation.copy(meta = Collections.unmodifiableMap(taskInformation.meta)),
      Collections.unmodifiableMap(payload),
      subscription.taskDescriptionKey,
      subscription.taskType
    )
    val report = RecordedDeliveryReport()
    TaskHandlerOutcomeRecorder.recordingFor(taskInformation.taskId) {
      val startedObservers = mutableListOf<StartedObserver<*>>()
      var deliveryError: Throwable? = null
      try {
        observers.forEach { observer -> start(observer, context)?.let(startedObservers::add) }
        delivery(report)
      } catch (e: Throwable) {
        deliveryError = e
        throw e
      } finally {
        val outcome = report.outcome ?: TaskHandlerOutcomeRecorder.recordedOutcome()
        val error = deliveryError ?: report.error
        startedObservers.asReversed().forEach { it.finish(context, outcome, error) }
      }
    }
  }

  private fun <S> start(observer: TaskHandlerObserver<S>, context: TaskHandlerContext): StartedObserver<S>? =
    logFailureOf({ "PROCESS-ENGINE-API-016: Observer failed before handler of task ${context.taskInformation.taskId}." }) {
      StartedObserver(observer, observer.before(context))
    }

  private class StartedObserver<S>(private val observer: TaskHandlerObserver<S>, private val state: S) {
    fun finish(context: TaskHandlerContext, outcome: TaskHandlerOutcome, error: Throwable?) {
      logFailureOf({ "PROCESS-ENGINE-API-017: Observer failed after handler of task ${context.taskInformation.taskId}." }) {
        observer.after(context, state, outcome, error)
      }
    }
  }

  private class RecordedDeliveryReport : TaskDeliveryReport {
    var outcome: TaskHandlerOutcome? = null
    var error: Throwable? = null

    override fun failed(error: Throwable) {
      this.error = error
      if (outcome != TaskHandlerOutcome.Vanished) {
        outcome = TaskHandlerOutcome.Failed(error.message ?: error.javaClass.name, null)
      }
    }

    override fun vanished(error: Throwable) {
      this.error = error
      outcome = TaskHandlerOutcome.Vanished
    }
  }
}

/**
 * Lets a delivery report to the [TaskHandlerObserver]s what happened to a task it handles itself,
 * without rethrowing. A reported outcome takes precedence over the one the handler reported through
 * the completion API, and [vanished] over [failed].
 * @since 1.8
 */
interface TaskDeliveryReport {
  /**
   * The delivery caught [error] and reported a failure of the task to the engine.
   */
  fun failed(error: Throwable)

  /**
   * The task was gone before the delivery could finish it, e.g. because its lock expired.
   */
  fun vanished(error: Throwable)

  companion object {
    /**
     * Report that discards everything, used when no observer runs.
     */
    @JvmField
    val IGNORED: TaskDeliveryReport = object : TaskDeliveryReport {
      override fun failed(error: Throwable) = Unit
      override fun vanished(error: Throwable) = Unit
    }
  }
}

private inline fun <T> logFailureOf(noinline message: () -> String, execution: () -> T): T? =
  try {
    execution()
  } catch (e: VirtualMachineError) {
    throw e
  } catch (e: Throwable) {
    logger.error(e, message)
    null
  }
