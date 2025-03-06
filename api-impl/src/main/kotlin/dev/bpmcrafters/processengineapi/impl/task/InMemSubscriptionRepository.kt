package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskType
import java.util.concurrent.ConcurrentHashMap

/**
 * Trivial in-memory implementation of the subscription repository.
 *
 * @since 0.0.1
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

  override fun getActiveSubscriptionForTask(taskId: String): TaskSubscriptionHandle? {
    return activeSubscribedHandler[taskId]
  }

  override fun activateSubscriptionForTask(taskId: String, subscription: TaskSubscriptionHandle) {
    this.activeSubscribedHandler[taskId] = subscription
  }

  override fun deactivateSubscriptionForTask(taskId: String): TaskSubscriptionHandle? {
    return this.activeSubscribedHandler.remove(taskId)
  }

  override fun getDeliveredTaskIds(taskType: TaskType) : List<String> {
    return this.activeSubscribedHandler.filter {
      it.value.taskType == taskType
    }.keys.toList()
  }

  fun deleteAllTaskSubscriptions() {
    subscriptions.clear()
    activeSubscribedHandler.clear()
  }

}
