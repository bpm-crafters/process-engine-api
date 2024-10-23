package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component // just to make the class open
class C7EmbeddedEmbeddedEventBasedUserTaskUserTaskDeliveryBinding(
  private val embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery,
  private val eventListenerOrder: Int = Ordered.HIGHEST_PRECEDENCE + 1000
) : Ordered, C7EmbeddedEventBasedUserTaskDelivery {

  override fun onTaskCreate(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskCreated(delegateTask = delegateTask)
  }

  override fun onTaskDelete(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskDeleted(delegateTask = delegateTask)
  }

  override fun getOrder(): Int = eventListenerOrder
}
