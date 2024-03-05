package dev.bpmcrafters.example.common.application.port.in;

import dev.bpmcrafters.processengineapi.task.TaskInformation;

import java.util.List;
import java.util.concurrent.Future;

public interface PerformUserTaskInPort {
  /**
   * Retrieves all user tasks.
   * @return list of tasks.
   */
  Future<List<TaskInformation>> getUserTasks();

  /**
   * Completes user task by id.
   * @param taskId task id.
   * @param value value to set.
   * @return future indicating completion.
   */
  Future<Void> complete(String taskId, String value);
  /**
   * Completes user task with error by id.
   * @param taskId task id.
   * @param value value to set.
   * @return future indicating completion.
   */
  Future<Void> completeWithError(String taskId, String value);
}
