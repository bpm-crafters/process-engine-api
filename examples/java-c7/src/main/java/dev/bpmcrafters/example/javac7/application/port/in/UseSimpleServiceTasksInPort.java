package dev.bpmcrafters.example.javac7.application.port.in;

import java.util.concurrent.Future;

public interface UseSimpleServiceTasksInPort {
  Future<String> execute(String value, Integer intValue);

  Future<Void> correlateMessage(String processInstanceId, String value);
}
