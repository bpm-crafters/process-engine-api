package dev.bpmcrafters.processengineapi.adapter.c7.remote.task

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.adapter.commons.task.AbstractTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository

class C7TaskSubscriptionApiImpl(
  subscriptionRepository: SubscriptionRepository
) : AbstractTaskSubscriptionApiImpl(
  subscriptionRepository = subscriptionRepository
) {

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }
}
