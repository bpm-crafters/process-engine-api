package dev.bpmcrafters.example.javac7.application.port;

import java.util.Map;
import java.util.concurrent.Future;

public interface PerformUserTaskInPort {
  Future<Map<String, Map<String, ?>>> getUserTasks();

  Future<Void> complete(String taskId, String value);
}
