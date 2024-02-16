package dev.bpmcrafters.example.javac7.application.port.in;

import dev.bpmcrafters.processengineapi.task.TaskInformation;

import java.util.List;
import java.util.concurrent.Future;

public interface PerformUserTaskInPort {
  Future<List<TaskInformation>> getUserTasks();

  Future<Void> complete(String taskId, String value);
}
