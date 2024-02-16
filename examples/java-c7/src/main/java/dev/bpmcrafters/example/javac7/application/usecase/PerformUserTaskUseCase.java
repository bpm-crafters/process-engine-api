package dev.bpmcrafters.example.javac7.application.usecase;

import dev.bpmcrafters.example.javac7.adapter.out.process.InMemUserTaskHandler;
import dev.bpmcrafters.example.javac7.application.port.in.PerformUserTaskInPort;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformUserTaskUseCase implements PerformUserTaskInPort {

  private final InMemUserTaskHandler taskPool;
  private final TaskApi taskApi;

  @Override
  public Future<List<TaskInformation>> getUserTasks() {
    log.info("Retrieving tasks");
    return CompletableFuture.completedFuture(taskPool.getAllTasks());
  }

  @Override
  @SneakyThrows
  public Future<Void> complete(String taskId, String value) {
    log.info("Completing task {}", taskId);
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      taskApi.completeTask(
        new CompleteTaskCmd(
          taskId,
          () -> Map.of("some-user-value", value)
        )
      ).get();
      completableFuture.complete(null); // FIXME -> Chain futures
      return null;
    });
    return completableFuture;
  }
}
