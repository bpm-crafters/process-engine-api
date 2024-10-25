package dev.bpmcrafters.example.common.application.port.out;

import dev.bpmcrafters.processengineapi.task.TaskInformation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Port to handle all user-task related activities and use cases.
 */
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

  /**
   * Completes the user task with given id and provided value.
   * @param taskId task id.
   * @param value value provided by the user.
   */
  void complete(String taskId, String value);

  /**
   * Throws a BPMN error on a given task.
   * @param taskId task id.
   * @param value value to store into payload during BPMN error throwing.
   */
  void completeWithError(String taskId, String value);
}
