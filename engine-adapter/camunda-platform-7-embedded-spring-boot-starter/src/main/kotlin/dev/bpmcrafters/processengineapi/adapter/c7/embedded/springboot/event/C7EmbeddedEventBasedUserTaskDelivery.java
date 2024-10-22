package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;

public interface C7EmbeddedEventBasedUserTaskDelivery {

  @EventListener(
    condition = "#delegateTask.eventName.equals('create')"
  )
  // @Async("c7embedded-user-task-worker-executor")
  void onTaskCreate(@NotNull DelegateTask delegateTask);

  @EventListener(
    condition = "#delegateTask.eventName.equals('delete') || #delegateTask.eventName.equals('timout')"
  )
  // @Async("c7embedded-user-task-worker-executor")
  void onTaskDelete(@NotNull DelegateTask delegateTask);
}
