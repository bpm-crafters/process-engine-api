package dev.bpmcrafters.example.javac7.adapter.in.rest;

import dev.bpmcrafters.example.javac7.application.port.PerformUserTaskInPort;
import dev.bpmcrafters.example.javac7.application.port.UseSimpleServiceTasksInPort;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/simple-service-tasks")
public class SimpleServiceTaskController {

  private final UseSimpleServiceTasksInPort useCase;
  private final PerformUserTaskInPort taskUseCase;

  @PostMapping("/start")
  @SneakyThrows
  public ResponseEntity<String> startUseCase(Dto dto) {
    val processInstanceId = useCase.execute(dto.value, dto.intValue).get();
    log.info("Started process instance {}", processInstanceId);
    return ok(processInstanceId);
  }

  @PostMapping("/correlate/{processInstanceId}")
  @SneakyThrows
  public ResponseEntity<Void> correlateMessage(@PathVariable("processInstanceId") String processInstanceId, String value) {
    useCase.correlateMessage(processInstanceId, value).get();
    log.info("Correlated message with process instance {}", processInstanceId);
    return noContent().build();
  }

  @GetMapping("/tasks")
  @SneakyThrows
  public ResponseEntity<Set<String>> getTasks() {
    return ok(taskUseCase.getUserTasks().get().keySet());
  }

  @PostMapping("/tasks/{taskId}")
  @SneakyThrows
  public ResponseEntity<Void> complete(@PathVariable("taskId") String taskId, String value) {
    taskUseCase.complete(taskId, value).get();
    return noContent().build();
  }

  public record Dto(
    String value,
    Integer intValue
  ) {
  }


}
