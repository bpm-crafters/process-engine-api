package dev.bpmcrafters.example.javac7.adapter.in.rest;

import dev.bpmcrafters.example.javac7.application.port.UseSimpleServiceTasksInPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SimpleServiceTaskController {

  private final UseSimpleServiceTasksInPort useCase;

  @PostMapping("/simple-service-tasks")
  @SneakyThrows
  public ResponseEntity<String> startUseCase(Dto dto) {
    val processInstanceId = useCase.execute(dto.getValue(), dto.getIntValue()).get();
    log.info("Started process instance {}", processInstanceId);
    return ok(processInstanceId);
  }

  @PostMapping("/simple-service-tasks/correlate/{processInstanceId}")
  @SneakyThrows
  public ResponseEntity<Void> correlateMessage(@PathVariable("processInstanceId") String processInstanceId, String value) {
    useCase.correlateMessage(processInstanceId, value).get();
    log.info("Correlated message with process instance {}", processInstanceId);
    return noContent().build();
  }

  @Data
  static class Dto {
    final String value;
    final Integer intValue;
  }


}
