package dev.bpmcrafters.example.javac7.application.usecase;

import dev.bpmcrafters.example.javac7.adapter.out.InMemUserTaskHandler;
import dev.bpmcrafters.example.javac7.application.port.PerformUserTaskInPort;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class PerformUserTaskUseCase implements PerformUserTaskInPort {

  private final InMemUserTaskHandler taskPool;
  private final TaskApi taskApi;

  @Override
  public Future<Map<String, Map<String, ?>>> getUserTasks() {
    return CompletableFuture.completedFuture(taskPool.getAllTasks());
  }

  @Override
  @SneakyThrows
  public Future<Void> complete(String taskId, String value) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      taskApi.completeTask(
        new CompleteTaskCmd(
          taskId,
          () -> Map.of("some-user-value", value)
        )
      ).get(); // FIXME -> Do we have a problem, Houston?
      return null;
    });
    return completableFuture;
  }
}
