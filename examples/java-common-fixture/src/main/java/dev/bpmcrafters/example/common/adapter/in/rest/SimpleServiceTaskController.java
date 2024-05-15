package dev.bpmcrafters.example.common.adapter.in.rest;

import dev.bpmcrafters.example.common.application.port.in.*;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/simple-service-tasks")
public class SimpleServiceTaskController {

  private final DeployInPort deployPort;
  private final StartProcessInstanceInPort processInstancePort;
  private final PerformUserTaskInPort taskPort;
  private final CorrelateInPort correlatePort;
  private final SignalInPort signalPort;

  @PostMapping("/deploy")
  @SneakyThrows
  public ResponseEntity<String> deploy() {
    val info = deployPort.deploy().get();
    log.info("Deployed process {}", info.getDeploymentKey());
    return created(URI.create(info.getDeploymentKey())).build();
  }

  @PostMapping("/start-process")
  @SneakyThrows
  public ResponseEntity<String> startUseCase(Dto dto) {
    val processInstanceId = processInstancePort.startNew(dto.value, dto.intValue).get();
    log.info("Started process instance {}", processInstanceId);
    return created(URI.create(processInstanceId)).build();
  }

  @PostMapping("/correlate/{correlationKey}")
  @SneakyThrows
  public ResponseEntity<Void> correlateMessage(@PathVariable("correlationKey") String correlationKey, String value) {
    log.info("Correlated message using correlation key {}", correlationKey);
    correlatePort.correlateMessage(correlationKey, value).get();
    return noContent().build();
  }

  @PostMapping("/signal")
  @SneakyThrows
  public ResponseEntity<Void> sendSignal(String value) {
    log.info("Sending signal");
    signalPort.deliverSignal(value).get();
    return noContent().build();
  }

  @GetMapping("/tasks")
  @SneakyThrows
  public ResponseEntity<List<TaskInformation>> getTasks() {
    return ok(taskPort.getUserTasks().get());
  }

  @PostMapping("/tasks/{taskId}/complete")
  @SneakyThrows
  public ResponseEntity<Void> complete(@PathVariable("taskId") String taskId, String value) {
    taskPort.complete(taskId, value).get();
    return noContent().build();
  }

  @PostMapping("/tasks/{taskId}/error")
  @SneakyThrows
  public ResponseEntity<Void> error(@PathVariable("taskId") String taskId, String value) {
    taskPort.completeWithError(taskId, value).get();
    return noContent().build();
  }

  public record Dto(
    String value,
    Integer intValue
  ) {
  }


}
