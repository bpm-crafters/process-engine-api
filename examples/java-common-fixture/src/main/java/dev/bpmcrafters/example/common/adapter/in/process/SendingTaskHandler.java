package dev.bpmcrafters.example.common.adapter.in.process;

import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SendingTaskHandler extends AbstractSynchronousTaskHandler {
  public SendingTaskHandler(TaskSubscriptionApi taskSubscriptionApi, ServiceTaskCompletionApi externalTaskCompletionApi) {
    super(taskSubscriptionApi, externalTaskCompletionApi, "send-message-external");
  }

  @Override
  public Map<String, Object> execute(TaskInformation taskInfo, Map<String, ?> variables) throws TaskHandlerException {
    log.info("[HANDLER SEND MESSAGE EXTERNAL]: Working on task {}", taskInfo.getTaskId());
    val map = new HashMap<String, Object>(variables);
    map.put("action2", "value2");
    return map;
  }
}
