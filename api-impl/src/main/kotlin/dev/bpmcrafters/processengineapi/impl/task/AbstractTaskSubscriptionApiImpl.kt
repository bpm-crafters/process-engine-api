package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.TaskType
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

/**
 * Abstract task subscription api implementation, using subscription repository and a list of completion strategies.
 * Each task handler subscribed for external tasks is observed by the given [TaskExecutionObserver]s.
 * @param observers observers of task executions, in notification order. Since 1.8.
 * @since 0.0.2
 */
abstract class AbstractTaskSubscriptionApiImpl @JvmOverloads constructor(
  private val subscriptionRepository: SubscriptionRepository,
  private val observers: List<TaskExecutionObserver> = emptyList()
) : TaskSubscriptionApi {

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): CompletableFuture<TaskSubscription> {
    return TaskSubscriptionHandle(
      taskDescriptionKey = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      restrictions = cmd.restrictions,
      action = observedAction(cmd),
      taskType = cmd.taskType,
      termination = cmd.termination
    ).let {
      subscriptionRepository.createTaskSubscription(it)
      logger.info { "PROCESS-ENGINE-API-001: Registered new task subscription $it" }
      CompletableFuture.completedFuture(it)
    }
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): CompletableFuture<Empty> {
    subscriptionRepository.deleteTaskSubscription(ensure(cmd.subscription))
    logger.info { "PROCESS-ENGINE-API-002: Un-registered new task subscription ${cmd.subscription}" }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun getSupportedRestrictions(): Set<String> = setOf()

  private fun observedAction(cmd: SubscribeForTaskCmd): TaskHandler =
    if (isObserved(cmd)) ObservedTaskHandler(cmd.action, observers) else cmd.action

  private fun isObserved(cmd: SubscribeForTaskCmd) = cmd.taskType == TaskType.EXTERNAL && observers.isNotEmpty()

  private fun ensure(subscription: TaskSubscription): TaskSubscriptionHandle {
    require(subscription is TaskSubscriptionHandle) {
      "Only subscriptions of type ${TaskSubscriptionHandle::class.java.name} are supported, but got ${subscription.javaClass.name}."
    }
    return subscription
  }

}
