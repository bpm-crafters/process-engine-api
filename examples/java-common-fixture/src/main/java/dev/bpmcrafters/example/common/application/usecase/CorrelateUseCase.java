package dev.bpmcrafters.example.common.application.usecase;

import dev.bpmcrafters.example.common.application.port.in.CorrelateInPort;
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd;
import dev.bpmcrafters.processengineapi.correlation.Correlation;
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Component
@RequiredArgsConstructor
public class CorrelateUseCase implements CorrelateInPort {

  private final CorrelationApi correlationApi;

  @Override
  public Future<Void> correlateMessage(String correlationValue, String variableValue) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        correlationApi.correlateMessage(
          new CorrelateMessageCmd(
            "message1",
            () -> Map.of(
              "message-delivered-value", variableValue
            ),
            () -> Correlation.withKey(correlationValue)
          )
        ).get();
        completableFuture.complete(null); // FIXME -> chain instead of sync get
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }
}
