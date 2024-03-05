package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.adapter.commons.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.AbstractTaskApiImpl

class C7TaskApiImpl(
  completionStrategies: List<CompletionStrategy>,
  subscriptionRepository: SubscriptionRepository
  ) : AbstractTaskApiImpl(
  completionStrategies = completionStrategies,
  subscriptionRepository = subscriptionRepository
) {

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }
}
