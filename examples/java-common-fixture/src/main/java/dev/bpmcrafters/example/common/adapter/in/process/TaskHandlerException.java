package dev.bpmcrafters.example.common.adapter.in.process;

import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Map;

@Getter
@ToString
public class TaskHandlerException extends RuntimeException {
  private final String errorCode;
  private final Map<String, Object> payload;

  public TaskHandlerException(String error, Map<String, Object> payload) {
    super("Error handling task resulting in error code " + error);
    this.errorCode = error;
    this.payload = payload;
  }

  public TaskHandlerException(String error) {
    this(error, Collections.emptyMap());
  }
}
