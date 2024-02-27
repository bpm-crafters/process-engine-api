package dev.bpmcrafters.example.javac7.application.usecase;

import dev.bpmcrafters.example.javac7.application.port.in.UseProcessInstanceInPort;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd;
import dev.bpmcrafters.processengineapi.correlation.Correlation;
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi;
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Component
@RequiredArgsConstructor
public class UseProcessInstanceUseCase implements UseProcessInstanceInPort {

  private final StartProcessApi startProcessApi;
  private final CorrelationApi correlationApi;

  @Override
  public Future<String> startNew(String value, Integer intValue) {
    CompletableFuture<String> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        val result = startProcessApi.startProcess(
          new StartProcessByDefinitionCmd(
            "simple-process-1",
            () -> Variables
              .createVariables()
              .putValue("stringValue", value)
              .putValue("intValue", intValue)
          )
        );
        completableFuture.complete(result.get().getInstanceId()); // FIXME -> chain instead of sync get
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }

  @Override
  public Future<Void> correlateMessage(String processInstanceId, String value) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        correlationApi.correlateMessage(
          new CorrelateMessageCmd(
            "message1",
            () -> Variables
              .createVariables()
              .putValue("message", value),
            () -> new Correlation(
              CommonRestrictions.builder().withInstanceId(processInstanceId).build()
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
  public Future<Void> deliverSignal(String value) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        correlationApi.sendSignal(
          new SendSignalCmd(
            "signal1",
            () -> Variables
              .createVariables()
              .putValue("signal", value),
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
