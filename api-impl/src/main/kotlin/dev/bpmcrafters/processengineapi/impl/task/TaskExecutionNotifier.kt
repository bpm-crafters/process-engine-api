package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionCleanup
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Notifies observers about tasks of one type; anything they throw is logged and ignored, except fatal JVM errors.
 */
internal class TaskExecutionNotifier(observers: List<TaskExecutionObserver>, private val taskType: TaskType) {

  private val observers = observers.toList()

  fun start(task: TaskInformation): List<TaskExecutionCleanup> =
    observers.mapNotNull { observer -> isolated(task.taskId) { observer.onStart(task, taskType) } }

  fun cleanUp(taskId: String, cleanups: List<TaskExecutionCleanup>) =
    cleanups.asReversed().forEach { cleanup -> isolated(taskId) { cleanup.close() } }

  fun outcome(taskId: String, outcome: TaskExecutionOutcome) =
    observers.forEach { observer -> isolated(taskId) { observer.onOutcome(taskId, taskType, outcome) } }

  fun termination(task: TaskInformation) =
    observers.forEach { observer -> isolated(task.taskId) { observer.onTermination(task, taskType) } }

  private fun <T> isolated(taskId: String, notification: () -> T): T? {
    return try {
      notification()
    } catch (e: VirtualMachineError) {
      throw e
    } catch (e: Throwable) {
      logger.error(e) { "PROCESS-ENGINE-API-016: Task execution observer failed for task $taskId." }
      null
    }
  }
}
