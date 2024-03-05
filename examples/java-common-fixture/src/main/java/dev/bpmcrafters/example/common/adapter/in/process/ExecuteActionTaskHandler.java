package dev.bpmcrafters.example.common.adapter.in.process;

import dev.bpmcrafters.processengineapi.task.TaskApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExecuteActionTaskHandler extends AbstractSynchronousTaskHandler {
  public ExecuteActionTaskHandler(TaskApi taskApi) {
    super(taskApi, "execute-action-external");
  }

  @Override
  public Map<String, Object> execute(TaskInformation taskInfo, Map<String, ?> variables) throws TaskHandlerException {
    log.info("[HANDLER EXECUTE ACTION EXTERNAL]: Working on task {}", taskInfo.getTaskId());
    Integer intValue = (Integer)variables.get("intValue");

    if (intValue % 42 == 0) {
      log.info("[HANDLER EXECUTE ACTION EXTERNAL]: Detected answer to the Ultimate Question of Life, the Universe, and Everything");
      throw new TaskHandlerException("action_error"); // will throw BPMN error
    }

    val map = new HashMap<String, Object>(variables);
    map.put("action1", "value1");
    return map;
  }
}
