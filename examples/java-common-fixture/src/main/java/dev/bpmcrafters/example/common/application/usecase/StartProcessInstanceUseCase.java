package dev.bpmcrafters.example.common.application.usecase;

import dev.bpmcrafters.example.common.application.port.in.StartProcessInstanceInPort;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Component
@RequiredArgsConstructor
public class StartProcessInstanceUseCase implements StartProcessInstanceInPort {

  private final StartProcessApi startProcessApi;

  @Override
  public Future<String> startNew(String value, Integer intValue) {
    CompletableFuture<String> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        val result = startProcessApi.startProcess(
          new StartProcessByDefinitionCmd(
            "simple-process-1",
            () -> Map.of(
              "stringValue", value,
              "intValue", intValue,
              "listVariable", List.of("element1", "element2")
            )
          )
        );
        completableFuture.complete(result.get().getInstanceId()); // FIXME -> chain instead of sync get
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }
}
