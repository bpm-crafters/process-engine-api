package dev.bpmcrafters.processengineapi.adapter.commons.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.*
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Abstract task api implementation, using subscription repository and a list of completion strategies.
 */
abstract class AbstractTaskApiImpl(
  private val completionStrategies: List<CompletionStrategy>,
  private val subscriptionRepository: SubscriptionRepository
) : TaskApi {

  companion object : KLogging()

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription> {
    // check that at least one completion strategy supports provided subscription restrictions
    require(completionStrategies.any { completionStrategy -> completionStrategy.supports(cmd.restrictions, cmd.taskDescriptionKey) })
    { "Subscription failed. provided restrictions (${cmd.restrictions}) are not supported by configured completion strategies " +
      "${completionStrategies.map { it.getSupportedRestrictions().joinToString(separator = ",", prefix = "'", postfix = "'") }}. " }
    return TaskSubscriptionHandle(
      taskDescriptionKey = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      restrictions = cmd.restrictions,
      action = cmd.action,
      modification = cmd.modification
    ).let {
      subscriptionRepository.createTaskSubscription(it)
      logger.info { "Registered new task subscription $it" }
      CompletableFuture.completedFuture(it)
    }
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Empty> {
    subscriptionRepository.deleteTaskSubscription(ensure(cmd.subscription))
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    // get active subscription
    val activeSubscription = subscriptionRepository.getActiveSubscriptionForTask(cmd.taskId)
    // find the correct strategy
    requireNotNull(activeSubscription) { "Could not complete task ${cmd.taskId}, task not found." }
    val strategy = completionStrategies.find { it.supports(restrictions = activeSubscription.restrictions, taskDescriptionKey = activeSubscription.taskDescriptionKey) }
    return strategy?.completeTask(cmd) ?: throw IllegalArgumentException("No completion strategy found for task ${cmd.taskId}")
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    // get active subscription
    val activeSubscription = subscriptionRepository.getActiveSubscriptionForTask(cmd.taskId)
    requireNotNull(activeSubscription) { "Could not complete task ${cmd.taskId} with error, task not found." }
    // find the correct strategy
    val strategy = completionStrategies.find { it.supports(restrictions = activeSubscription.restrictions, taskDescriptionKey = activeSubscription.taskDescriptionKey) }
    return strategy?.completeTaskByError(cmd) ?: throw IllegalArgumentException("No completion strategy found for task ${cmd.taskId}")
  }

  override fun getSupportedRestrictions(): Set<String> = this.completionStrategies.map { it.getSupportedRestrictions() }.flatten().toSet()

  private fun ensure(subscription: TaskSubscription): TaskSubscriptionHandle {
    require(subscription is TaskSubscriptionHandle) {
      "Only subscriptions of type ${TaskSubscriptionHandle::class.java.name} are supported, but got ${subscription.javaClass.name}."
    }
    return subscription
  }

}
