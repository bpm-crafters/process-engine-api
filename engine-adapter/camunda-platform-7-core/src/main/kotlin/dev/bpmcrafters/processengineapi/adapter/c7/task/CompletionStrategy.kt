package dev.bpmcrafters.processengineapi.adapter.c7.task

import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import java.util.concurrent.Future

/**
 * Represents a strategy for task completion.
 */
interface CompletionStrategy {

  /**
   * Retrieves a set of supported restrictions.
   * @return set of restrictions.
   */
  fun getSupportedRestrictions(): Set<String> = setOf()

  /**
   * Checks whether the strategy supports restrictions and provided task description.
   * @param restrictions restrictions of the subscriber.
   * @param taskDescriptionKey key of current task.
   * @return true if supported completion of this task.
   */
  fun supports(restrictions: Map<String, String>, taskDescriptionKey: String?): Boolean = false

  /**
   * Completes task.
   * @param cmd completion command.
   * @return completion future.
   */
  fun completeTask(cmd: CompleteTaskCmd): Future<Unit>

  /**
   * Completes task throwing an error.
   * @param cmd completion command.
   * @return completion future.
   */
  fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Unit>
}
