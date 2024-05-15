package dev.bpmcrafters.processengineapi.adapter.c8.task.subscription

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.adapter.c8.task.SubscribingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.AbstractTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class C8TaskSubscriptionApiImpl(
  subscriptionRepository: SubscriptionRepository,
  private val subscribingUserTaskDelivery: SubscribingUserTaskDelivery?
) : AbstractTaskSubscriptionApiImpl(
  subscriptionRepository = subscriptionRepository
) {

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Empty> {
    super.unsubscribe(cmd)
    // For subscribing delivery, we also have to close the job
    subscribingUserTaskDelivery?.unsubscribe(cmd.subscription)
    return CompletableFuture.completedFuture(Empty)
  }
}
