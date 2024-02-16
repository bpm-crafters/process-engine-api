package dev.bpmcrafters.example.javac7.application.port.out;

import dev.bpmcrafters.processengineapi.task.TaskInformation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserTaskOutPort {

  List<TaskInformation> getAllTasks();

  Optional<Map<String, ?>> getTaskVariablesById(String taskId);
}
