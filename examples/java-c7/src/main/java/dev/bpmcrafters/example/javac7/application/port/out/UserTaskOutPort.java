package dev.bpmcrafters.example.javac7.application.port.out;

import dev.bpmcrafters.processengineapi.task.TaskInformation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserTaskOutPort {

  /**
   * Retrieves all user tasks.
   * @return user tasks.
   */
  List<TaskInformation> getAllTasks();

  /**
   * Loads variables for a task.
   * @param taskId task id.
   * @return process variable map.
   */
  Optional<Map<String, ?>> getTaskVariablesById(String taskId);
}
