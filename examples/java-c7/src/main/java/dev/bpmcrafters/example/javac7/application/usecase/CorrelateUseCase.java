package dev.bpmcrafters.example.javac7.application.usecase;

import dev.bpmcrafters.example.common.application.port.in.CorrelateInPort;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd;
import dev.bpmcrafters.processengineapi.correlation.Correlation;
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi;
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd;
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
            () -> new Correlation(
              CommonRestrictions.builder().withInstanceId(correlationValue).build() // FIXME: works in C7 und not in C8
            )
          )
        ).get();
        completableFuture.complete(null); // FIXME -> chain instead of sync get
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }

  @Override
  public Future<Void> deliverSignal(String variableValue) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        correlationApi.sendSignal(
          new SendSignalCmd(
            "signal1",
            () -> Map.of(
              "signal-delivered-value", variableValue
            ),
            Correlation.Companion::getEMPTY
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
