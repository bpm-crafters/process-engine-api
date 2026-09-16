package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptorChain
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptorContext
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType

/**
 * Wraps a [TaskHandler] and runs a list of [TaskHandlerInterceptor]s around its execution.
 * Interceptors run in the order of the given list; the wrapped [delegate] is invoked last.
 * @since 1.7
 */
class InterceptingTaskHandler(
  private val delegate: TaskHandler,
  private val interceptors: List<TaskHandlerInterceptor>,
  private val taskDescriptionKey: String?,
  private val taskType: TaskType
) : TaskHandler {

  override fun accept(taskInformation: TaskInformation, payload: Map<String, Any?>) {
    val context = TaskHandlerInterceptorContext(taskInformation, payload, taskDescriptionKey, taskType)
    proceed(context, 0, taskInformation, payload)
  }

  private fun proceed(
    context: TaskHandlerInterceptorContext,
    index: Int,
    taskInformation: TaskInformation,
    payload: Map<String, Any?>
  ) {
    if (index < interceptors.size) {
      interceptors[index].intercept(context) { proceed(context, index + 1, taskInformation, payload) }
    } else {
      delegate.accept(taskInformation, payload)
    }
  }
}
