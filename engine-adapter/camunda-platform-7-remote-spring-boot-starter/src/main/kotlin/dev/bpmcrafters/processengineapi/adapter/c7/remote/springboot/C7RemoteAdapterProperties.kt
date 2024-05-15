package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.Companion.DEFAULT_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = DEFAULT_PREFIX)
class C7RemoteAdapterProperties(
  /**
   * Configuration for external service tasks.
   */
  @NestedConfigurationProperty
  val externalServiceTasks: ExternalServiceTasks,

  /**
   * Configuration of user tasks.
   */
  @NestedConfigurationProperty
  val userTasks: UserTasks
) {

  companion object {
    const val DEFAULT_PREFIX = "dev.bpm-crafters.process-api.adapter.c7.remote"
  }

  /**
   * Configuration for user task handling.
   */
  data class UserTasks(
    /**
     * Delivery strategy for user tasks.
     */
    val deliveryStrategy: UserTaskDeliveryStrategy,
    /**
     * Fixed rate for scheduled user task delivery.
     */
    val fixedRateScheduleRate: Long = 5_000L
  )

  /**
   * Configuration for external service task handling.
   */
  data class ExternalServiceTasks(
    /**
     * Default id of the worker used for the external task.
     */
    val workerId: String,
    /**
     * Max count of external tasks to fetch. Defaults to 100.
     */
    val maxTaskCount: Int = 100,
    /**
     * Time in seconds to lock external task. Default to 10.
     */
    val lockTimeInSeconds: Long = 10L,
    /**
     * Retry timout in seconds.
     */
    val retryTimeoutInSeconds: Long = 10L,
    /**
     * Fixed rate for scheduled user task delivery.
     */
    val fixedRateScheduleRate: Long = 13_000L,
    /**
     * Delivery strategy for external service tasks.
     */
    val deliveryStrategy: ExternalServiceTaskDeliveryStrategy
  )

  /**
   * Strategy how the user tasks are delivered to subscriptions.
   */
  enum class UserTaskDeliveryStrategy {

    /**
     * Delivery via scheduler.
     */
    REMOTE_SCHEDULED,

    /**
     * Custom delivery.
     */
    CUSTOM,

    /**
     * Disabled delivery
     */
    DISABLED
  }


  /**
   * Strategy how the external service tasks are delivered to subscriptions.
   */
  enum class ExternalServiceTaskDeliveryStrategy {
    /**
     * Delivery via scheduler.
     */
    REMOTE_SCHEDULED,

    /**
     * Delivery via subscription of the official external task client.
     */
    REMOTE_SUBSCRIBED,

    /**
     * Custom delivery.
     */
    CUSTOM,

    /**
     * Disabled delivery
     */
    DISABLED
  }
}
