package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import java.util.concurrent.CompletableFuture

internal class NoOpServiceTaskCompletionApi : ServiceTaskCompletionApi {
  override fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty> = CompletableFuture.completedFuture(Empty)
  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): CompletableFuture<Empty> = CompletableFuture.completedFuture(Empty)
  override fun failTask(cmd: FailTaskCmd): CompletableFuture<Empty> = CompletableFuture.completedFuture(Empty)
}
