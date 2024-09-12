package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration of Camunda 8 adapter.
 */
@ConfigurationProperties(prefix = DEFAULT_PREFIX)
class C8AdapterProperties(
  /**
   * Flag controlling if the entire adapter is active.
   */
  val enabled: Boolean = true,
  /**
   * Configuration for external service tasks.
   */
  @NestedConfigurationProperty
  val serviceTasks: ServiceTasks,

  /**
   * Configuration of user tasks.
   */
  @NestedConfigurationProperty
  val userTasks: UserTasks
) {

  companion object {
    const val DEFAULT_PREFIX = "dev.bpm-crafters.process-api.adapter.c8"
  }

  class ServiceTasks(
    /**
     * Delivery strategy for user tasks.
     */
    val deliveryStrategy: ServiceTaskDeliveryStrategy,
    /**
     * Default id of the worker used for the external task.
     */
    val workerId: String,
  )

  data class UserTasks(
    /**
     * Delivery strategy for user tasks.
     */
    val deliveryStrategy: UserTaskDeliveryStrategy,
    /**
     * Completion strategy for user tasks.
     */
    val completionStrategy: UserTaskCompletionStrategy,
    /**
     * Fixed rate for scheduled user task delivery.
     */
    val scheduleDeliveryFixedRateInSeconds: Long = 5L,
    /**
     * Fixed rate for refreshing user task delivery.
     */
    val subscribingDeliveryInitialDelayInSeconds: Long = 5L,
    /**
     * URL of the task list.
     */
    val tasklistUrl: String = "https://\${zeebe.client.cloud.region}.tasklist.camunda.io/\${zeebe.client.cloud.clusterId}"
  )

  /**
   * Strategy to complete user tasks.
   */
  enum class UserTaskCompletionStrategy {
    /**
     * Use task list client.
     */
    TASKLIST,

    /**
     * Use zeebe client.
     */
    JOB
  }

  /**
   * Strategy to deliver user tasks.
   */
  enum class UserTaskDeliveryStrategy {
    /**
     * Scheduled, based on task list client.
     */
    SCHEDULED,

    /**
     * Subscribing using zeebe job subscriptions, extending lock times.
     */
    SUBSCRIPTION_REFRESHING
  }

  /**
   * Strategy to deliver external service tasks.
   */
  enum class ServiceTaskDeliveryStrategy {
    /**
     * Subscribing using zeebe job.
     */
    SUBSCRIPTION
  }
}
