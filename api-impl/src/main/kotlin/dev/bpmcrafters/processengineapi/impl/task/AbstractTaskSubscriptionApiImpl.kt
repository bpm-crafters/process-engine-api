package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

/**
 * Abstract task subscription api implementation, using subscription repository and a list of completion strategies.
 * The handlers of each subscription are observed by the given [TaskExecutionObserver]s.
 * @param observers observers of the subscribed tasks, in notification order. Since 1.8.
 * @since 0.0.2
 */
abstract class AbstractTaskSubscriptionApiImpl @JvmOverloads constructor(
  private val subscriptionRepository: SubscriptionRepository,
  private val observers: List<TaskExecutionObserver> = emptyList()
) : TaskSubscriptionApi {

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): CompletableFuture<TaskSubscription> {
    ensureSupported(cmd.restrictions)
    require(
      !(cmd.restrictions.containsKey(CommonRestrictions.TENANT_ID) &&
        cmd.restrictions.containsKey(CommonRestrictions.WITHOUT_TENANT_ID))
    ) {
      "Illegal restriction combination. ${CommonRestrictions.TENANT_ID} and " +
        "${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided at the same time because they are mutually exclusive."
    }
    return TaskSubscriptionHandle(
      taskDescriptionKey = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      restrictions = cmd.restrictions,
      action = observedAction(cmd),
      taskType = cmd.taskType,
      termination = observedTermination(cmd)
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

  /**
   * Task delivery is adapter-specific. Implementations must declare the restrictions which are
   * guaranteed to work for every delivery strategy they expose.
   */
  abstract override fun getSupportedRestrictions(): Set<String>

  private fun observedAction(cmd: SubscribeForTaskCmd): TaskHandler {
    if (observers.isEmpty()) {
      return cmd.action
    }
    return ObservedTaskHandler(cmd.action, cmd.taskType, observers)
  }

  private fun observedTermination(cmd: SubscribeForTaskCmd): TaskTerminationHandler {
    if (observers.isEmpty()) {
      return cmd.termination
    }
    return ObservedTaskTerminationHandler(cmd.termination, cmd.taskType, observers)
  }

  private fun ensure(subscription: TaskSubscription): TaskSubscriptionHandle {
    require(subscription is TaskSubscriptionHandle) {
      "Only subscriptions of type ${TaskSubscriptionHandle::class.java.name} are supported, but got ${subscription.javaClass.name}."
    }
    return subscription
  }

}
