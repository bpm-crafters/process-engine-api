package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.Empty
import java.util.concurrent.CompletableFuture

/**
 * API for completion of user tasks.
 * @since 0.0.1
 */
interface UserTaskCompletionApi {
  /**
   * Complete the task.
   * @param cmd command to complete the task.
   * @return future indicating the completion.
   */
  fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty>

  /**
   * Completes the task by throwing an BPMN error.
   * @param cmd command to complete the task.
   * @return future indicating the completion.
   */
  fun completeTaskByError(cmd: CompleteTaskByErrorCmd): CompletableFuture<Empty>
}
