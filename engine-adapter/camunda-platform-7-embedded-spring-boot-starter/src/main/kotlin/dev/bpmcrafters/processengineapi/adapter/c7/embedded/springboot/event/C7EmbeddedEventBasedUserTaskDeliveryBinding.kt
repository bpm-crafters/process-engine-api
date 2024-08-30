package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event.C7EmbeddedEventBasedUserTaskDeliveryBinding.Companion.ORDER
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Order(ORDER)
class C7EmbeddedEventBasedUserTaskDeliveryBinding(
  private val embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery
) {

  companion object {
    const val ORDER = Ordered.HIGHEST_PRECEDENCE + 1000
  }

  @EventListener(
    condition = "#delegateTask.eventName.equals('create')"
  )
  fun onTaskCreate(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskCreated(delegateTask = delegateTask)
  }

  @EventListener(
    condition = "#delegateTask.eventName.equals('delete') || #delegateTask.eventName.equals('timout')"
  )
  fun onTaskDelete(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskDeleted(delegateTask = delegateTask)
  }
}
