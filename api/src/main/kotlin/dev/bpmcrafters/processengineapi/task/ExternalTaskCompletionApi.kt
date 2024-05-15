package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.Empty
import java.util.concurrent.Future

/**
 * API for completion of service tasks.
 * @since 0.0.1
 */
interface ExternalTaskCompletionApi {
  /**
   * Complete the task.
   * @param cmd command to complete the task.
   * @return future indicating the completion.
   */
  fun completeTask(cmd: CompleteTaskCmd): Future<Empty>

  /**
   * Completes the task by throwing an BPMN error.
   * @param cmd command to complete the task.
   * @return future indicating the completion.
   */
  fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty>

  /**
   * Failes to complete the task because of a technical failure.
   * @param cmd command to indicate failure.
   * @return future indicating the failure.
   */
  fun failTask(cmd: FailTaskCmd): Future<Empty>

}
