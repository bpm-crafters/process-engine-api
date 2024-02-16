package dev.bpmcrafters.processengineapi.adapter.c7.task

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.task.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class TaskApiImpl(
  private val completionStrategies: List<CompletionStrategy>,
  private val subscriptionRepository: SubscriptionRepository
) : TaskApi {

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription> {
    return TaskSubscriptionHandle(
      taskDescriptionKey = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      action = cmd.action,
      restrictions = cmd.restrictions
    ).let {
      subscriptionRepository.createTaskSubscription(it)
      CompletableFuture.completedFuture(it)
    }
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Unit> {
    subscriptionRepository.deleteTaskSubscription(ensure(cmd.subscription))
    return CompletableFuture.completedFuture(null)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Unit> {
    // get active subscription
    val activeSubscription = subscriptionRepository.getActiveSubscriptionForTask(cmd.taskId)
    // find the correct strategy
    val strategy = completionStrategies.find { it.supports(restrictions = activeSubscription.restrictions, taskDescriptionKey = activeSubscription.taskDescriptionKey) }
    return strategy?.completeTask(cmd) ?: throw IllegalArgumentException("no completion strategy found for task ${cmd.taskId}")
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Unit> {
    // get active subscription
    val activeSubscription = subscriptionRepository.getActiveSubscriptionForTask(cmd.taskId)
    // find the correct strategy
    val strategy = completionStrategies.find { it.supports(restrictions = activeSubscription.restrictions, taskDescriptionKey = activeSubscription.taskDescriptionKey) }
    return strategy?.completeTaskByError(cmd) ?: throw IllegalArgumentException("no completion strategy found for task ${cmd.taskId}")
  }

  override fun getSupportedRestrictions(): Set<String> = this.completionStrategies.map { it.getSupportedRestrictions() }.flatten().toSet()

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  private fun ensure(subscription: TaskSubscription): TaskSubscriptionHandle {
    require(subscription is TaskSubscriptionHandle) {
      "Only subscriptions of type ${TaskSubscriptionHandle::class.java.name} are supported, but got ${subscription.javaClass.name}."
    }
    return subscription
  }

}
