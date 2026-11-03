package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandlerContext
import dev.bpmcrafters.processengineapi.task.TaskHandlerObserver
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections

private val logger = KotlinLogging.logger {}

/**
 * Runs the [delegate] exactly once, calling [TaskHandlerObserver.before] in list order before it
 * and [TaskHandlerObserver.after] in reverse order after it. Everything an observer throws is only
 * logged, so the delivery sees the same result as without observers. Only a [VirtualMachineError]
 * propagates, because the JVM cannot be trusted afterwards. The thread context class loader is the
 * delegate's, because adapters would otherwise derive it from this wrapper.
 * @since 1.8
 */
internal class ObserverAwareTaskHandler(
  private val delegate: TaskHandler,
  private val observers: List<TaskHandlerObserver<*>>,
  private val taskDescriptionKey: String?,
  private val taskType: TaskType
) : TaskHandler {

  override fun accept(taskInformation: TaskInformation, payload: Map<String, Any?>) {
    val context = TaskHandlerContext(
      taskInformation.copy(meta = Collections.unmodifiableMap(taskInformation.meta)),
      Collections.unmodifiableMap(payload),
      taskDescriptionKey,
      taskType
    )
    withDelegateContextClassLoader {
      TaskHandlerOutcomeRecorder.recordingFor(taskInformation.taskId) {
        val startedObservers = mutableListOf<StartedObserver<*>>()
        var handlerError: Throwable? = null
        try {
          observers.forEach { observer -> start(observer, context)?.let(startedObservers::add) }
          delegate.accept(taskInformation, payload)
        } catch (e: Throwable) {
          handlerError = e
          throw e
        } finally {
          val outcome = TaskHandlerOutcomeRecorder.recordedOutcome()
          startedObservers.asReversed().forEach { it.finish(context, outcome, handlerError) }
        }
      }
    }
  }

  private fun <S> start(observer: TaskHandlerObserver<S>, context: TaskHandlerContext): StartedObserver<S>? =
    logFailureOf({ "PROCESS-ENGINE-API-016: Observer failed before handler of task ${context.taskInformation.taskId}." }) {
      StartedObserver(observer, observer.before(context))
    }

  private class StartedObserver<S>(private val observer: TaskHandlerObserver<S>, private val state: S) {
    fun finish(context: TaskHandlerContext, outcome: TaskHandlerOutcome, handlerError: Throwable?) {
      logFailureOf({ "PROCESS-ENGINE-API-017: Observer failed after handler of task ${context.taskInformation.taskId}." }) {
        observer.after(context, state, outcome, handlerError)
      }
    }
  }

  private fun withDelegateContextClassLoader(execution: () -> Unit) {
    val thread = Thread.currentThread()
    val originalClassLoader = thread.contextClassLoader
    val delegateClassLoader = delegate.javaClass.classLoader ?: return execution()
    thread.contextClassLoader = delegateClassLoader
    try {
      execution()
    } finally {
      thread.contextClassLoader = originalClassLoader
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
