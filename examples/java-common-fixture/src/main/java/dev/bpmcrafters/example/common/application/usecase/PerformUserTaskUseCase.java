package dev.bpmcrafters.example.common.application.usecase;

import dev.bpmcrafters.example.common.application.port.out.UserTaskOutPort;
import dev.bpmcrafters.example.common.application.port.in.PerformUserTaskInPort;
import dev.bpmcrafters.processengineapi.task.*;
import lombok.RequiredArgsConstructor;
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

  private final UserTaskOutPort userTaskPort;

  @Override
  public Future<List<TaskInformation>> getUserTasks() {
    log.info("Retrieving tasks");
    return CompletableFuture.completedFuture(userTaskPort.getAllTasks());
  }

  @Override
  public Future<Void> complete(String taskId, String value) {
    log.info("Completing task {}", taskId);
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        userTaskPort.complete(taskId, value);
        completableFuture.complete(null); // FIXME -> Chain futures
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }

  @Override
  public Future<Void> completeWithError(String taskId, String value) {
    // FIXME -> currently not working in C8: no API in the task-list-client!
    log.info("Completing task {} with error", taskId);
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        userTaskPort.completeWithError(taskId, value);
        completableFuture.complete(null); // FIXME -> Chain futures
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }
}
