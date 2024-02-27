package dev.bpmcrafters.processengineapi.adapter.c7.task

/**
 * Repository for managing subscriptions.
 */
interface SubscriptionRepository {

  fun getTaskSubscriptions(): List<TaskSubscriptionHandle>
  fun createTaskSubscription(subscription: TaskSubscriptionHandle)
  fun deleteTaskSubscription(subscription: TaskSubscriptionHandle)

  fun activateSubscriptionForTask(taskId: String, subscription: TaskSubscriptionHandle)
  fun getActiveSubscriptionForTask(taskId: String): TaskSubscriptionHandle?
  fun removeSubscriptionForTask(taskId: String): TaskSubscriptionHandle?
}
