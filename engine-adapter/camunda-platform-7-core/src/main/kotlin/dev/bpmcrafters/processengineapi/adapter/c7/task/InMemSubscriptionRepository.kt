package dev.bpmcrafters.processengineapi.adapter.c7.task

import java.util.concurrent.ConcurrentHashMap

/**
 * Trivial in-memory implementation of the repository.
 */
class InMemSubscriptionRepository : SubscriptionRepository {

  private val subscriptions: MutableList<TaskSubscriptionHandle> = mutableListOf()
  private val activeSubscribedHandler: ConcurrentHashMap<String, TaskSubscriptionHandle> = ConcurrentHashMap()

  override fun getTaskSubscriptions(): List<TaskSubscriptionHandle> = subscriptions.toList()

  override fun createTaskSubscription(subscription: TaskSubscriptionHandle) {
    subscriptions.add(subscription)
  }

  override fun deleteTaskSubscription(subscription: TaskSubscriptionHandle) {
    subscriptions.remove(subscription)
  }

  override fun getActiveSubscriptionForTask(taskId: String): TaskSubscriptionHandle {
    return activeSubscribedHandler[taskId] ?: throw IllegalArgumentException("No active subscription for for task $taskId found.")
  }

  override fun activateSubscriptionForTask(taskId: String, subscription: TaskSubscriptionHandle) {
    this.activeSubscribedHandler[taskId] = subscription
  }

  override fun removeSubscriptionForTask(taskId: String) {
    this.activeSubscribedHandler.remove(taskId)
  }

}
