package dev.bpmcrafters.example.common.adapter.in.process;

import dev.bpmcrafters.processengineapi.task.TaskApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SendingTaskHandler extends AbstractSynchronousTaskHandler {
  public SendingTaskHandler(TaskApi taskApi) {
    super(taskApi, "send-message-external");
  }

  @Override
  public Map<String, Object> execute(TaskInformation taskInfo, Map<String, ?> variables) throws TaskHandlerException {
    log.info("[HANDLER SEND MESSAGE EXTERNAL]: Working on task {}", taskInfo.getTaskId());
    val map = new HashMap<String, Object>(variables);
    map.put("action2", "value2");
    return map;
  }
}
