package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = DEFAULT_PREFIX)
class C8AdapterProperties(
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
     * Fixed rate for scheduled user task delivery.
     */
    val fixedRateScheduleRate: Long = 5_000L,
    /**
     * Fixed rate for refreshing user task delivery
     */
    val fixedRateRefreshRate: Long = 5_000L,
    /**
     * URL of the task list.
     */
    val tasklistUrl: String
  )

  enum class UserTaskDeliveryStrategy {
    SCHEDULED,
    SUBSCRIPTION_REFRESHING
  }

  enum class ServiceTaskDeliveryStrategy {
    SUBSCRIPTION
  }
}
