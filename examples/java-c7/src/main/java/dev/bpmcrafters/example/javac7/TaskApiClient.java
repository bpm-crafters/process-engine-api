package dev.bpmcrafters.example.javac7;

import dev.bpmcrafters.processengineapi.task.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static dev.bpmcrafters.processengineapi.CommonRestrictions.builder;

/**
 * Example client.
 */
public class TaskApiClient {
  private final TaskApi taskApi;
  private final Map<String, TaskSubscription> activeSubscriptions = new HashMap<>();

  public TaskApiClient(TaskApi taskApi) {
    this.taskApi = taskApi;
  }

  public void registerForTask(String processDefinitionKey, String taskDefinitionKey, TaskHandler action) throws InterruptedException, ExecutionException {
    TaskSubscription subscription = taskApi.subscribeForTask(
      new SubscribeForTaskCmd(
        taskApi.ensureSupported(builder().withProcessDefinitionKey(processDefinitionKey).build()),
        taskDefinitionKey, Collections.emptySet(), action
      )
    ).get();
    activeSubscriptions.put(processDefinitionKey + taskDefinitionKey, subscription);
  }

  public void completeTask(String taskId, Map<String, Object> payload) throws InterruptedException, ExecutionException {
    taskApi.completeTask(new CompleteTaskCmd(taskId, () -> payload)).get();
  }

  public void completeTaskWithError(String taskId, String error) throws InterruptedException, ExecutionException {
    taskApi.completeTaskByError(new CompleteTaskByErrorCmd(taskId, error)).get();
  }

  public void unsubscribe() {
    activeSubscriptions.forEach((name, taskSubscription) -> taskApi.unsubscribe(new UnsubscribeFromTaskCmd(taskSubscription)));
  }
}
