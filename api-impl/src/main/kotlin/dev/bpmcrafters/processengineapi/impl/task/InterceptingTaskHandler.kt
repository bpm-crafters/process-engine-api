package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptorContext
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections

private val logger = KotlinLogging.logger {}

/**
 * Runs [TaskHandlerInterceptor]s in list order around the [delegate]. The thread context class
 * loader is the delegate's, because adapters would otherwise derive it from this wrapper.
 *
 * An interceptor exception after the handler returned is deliberately only logged: the task may be
 * finished, so the delivery must not report a failure for it.
 * @since 1.8
 */
internal class InterceptingTaskHandler(
  private val delegate: TaskHandler,
  private val interceptors: List<TaskHandlerInterceptor>,
  private val taskDescriptionKey: String?,
  private val taskType: TaskType
) : TaskHandler {

  override fun accept(taskInformation: TaskInformation, payload: Map<String, Any?>) {
    val context = TaskHandlerInterceptorContext(taskInformation, Collections.unmodifiableMap(payload), taskDescriptionKey, taskType)
    var handlerReturned = false

    fun proceed(index: Int): TaskHandlerOutcome {
      TaskHandlerOutcomeRecorder.clearRecordedOutcome()
      if (index < interceptors.size) {
        interceptors[index].intercept(context) { proceed(index + 1) }
      } else {
        handlerReturned = false
        delegate.accept(taskInformation, payload)
        handlerReturned = true
      }
      return TaskHandlerOutcomeRecorder.recordedOutcome()
    }

    withDelegateContextClassLoader {
      TaskHandlerOutcomeRecorder.recordingFor(taskInformation.taskId) {
        try {
          proceed(0)
        } catch (e: Exception) {
          if (!handlerReturned) throw e
          logger.error(e) { "PROCESS-ENGINE-API-016: Interceptor failed after handler of task ${taskInformation.taskId} returned." }
        }
      }
    }
  }

  private fun withDelegateContextClassLoader(execution: () -> Unit) {
    val thread = Thread.currentThread()
    val originalClassLoader = thread.contextClassLoader
    thread.contextClassLoader = delegate.javaClass.classLoader
    try {
      execution()
    } finally {
      thread.contextClassLoader = originalClassLoader
    }
  }
}
