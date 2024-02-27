package dev.bpmcrafters.example.javac7.adapter.in.rest;

import dev.bpmcrafters.example.javac7.application.port.in.PerformUserTaskInPort;
import dev.bpmcrafters.example.javac7.application.port.in.UseProcessInstanceInPort;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/simple-service-tasks")
public class SimpleServiceTaskController {

  private final UseProcessInstanceInPort processInstancePort;
  private final PerformUserTaskInPort taskPort;

  @PostMapping("/start")
  @SneakyThrows
  public ResponseEntity<String> startUseCase(Dto dto) {
    val processInstanceId = processInstancePort.startNew(dto.value, dto.intValue).get();
    log.info("Started process instance {}", processInstanceId);
    return ok(processInstanceId);
  }

  @PostMapping("/correlate/{processInstanceId}")
  @SneakyThrows
  public ResponseEntity<Void> correlateMessage(@PathVariable("processInstanceId") String processInstanceId, String value) {
    processInstancePort.correlateMessage(processInstanceId, value).get();
    log.info("Correlated message with process instance {}", processInstanceId);
    return noContent().build();
  }

  @PostMapping("/signal")
  @SneakyThrows
  public ResponseEntity<Void> sendSignal(String value) {
    processInstancePort.deliverSignal(value).get();
    log.info("Sending signal");
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
