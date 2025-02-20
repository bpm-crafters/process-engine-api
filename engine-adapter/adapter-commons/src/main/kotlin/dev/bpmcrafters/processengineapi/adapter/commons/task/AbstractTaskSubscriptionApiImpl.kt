package dev.bpmcrafters.processengineapi.adapter.commons.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

/**
 * Abstract task subscription api implementation, using subscription repository and a list of completion strategies.
 * @since 0.0.2
 */
abstract class AbstractTaskSubscriptionApiImpl(
  private val subscriptionRepository: SubscriptionRepository
) : TaskSubscriptionApi {

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription> {
    return TaskSubscriptionHandle(
      taskDescriptionKey = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      restrictions = cmd.restrictions,
      action = cmd.action,
      taskType = cmd.taskType,
      termination = cmd.termination
    ).let {
      subscriptionRepository.createTaskSubscription(it)
      logger.info { "PROCESS-ENGINE-API-001: Registered new task subscription $it" }
      CompletableFuture.completedFuture(it)
    }
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Empty> {
    subscriptionRepository.deleteTaskSubscription(ensure(cmd.subscription))
    logger.info { "PROCESS-ENGINE-API-002: Un-registered new task subscription ${cmd.subscription}" }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun getSupportedRestrictions(): Set<String> = setOf()

  private fun ensure(subscription: TaskSubscription): TaskSubscriptionHandle {
    require(subscription is TaskSubscriptionHandle) {
      "Only subscriptions of type ${TaskSubscriptionHandle::class.java.name} are supported, but got ${subscription.javaClass.name}."
    }
    return subscription
  }

}
