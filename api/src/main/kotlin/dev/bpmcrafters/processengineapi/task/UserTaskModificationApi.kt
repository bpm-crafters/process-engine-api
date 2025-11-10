package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.Empty
import java.util.concurrent.CompletableFuture

/**
 * API for modification of user tasks.
 * @since 1.3
 */
interface UserTaskModificationApi {

  /**
   * Modifies user task.
   * @param cmd command describing the modification.
   * @return future indicating the completion.
   */
  fun update(cmd: ModifyTaskCmd): CompletableFuture<Empty>
}
