package dev.bpmcrafters.example.javac7.infrastructure;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TaskHandlerException extends RuntimeException {
  private final String errorCode;

  public TaskHandlerException(String error) {
    super("Error handling task resulting in error code " + error);
    this.errorCode = error;
  }
}
