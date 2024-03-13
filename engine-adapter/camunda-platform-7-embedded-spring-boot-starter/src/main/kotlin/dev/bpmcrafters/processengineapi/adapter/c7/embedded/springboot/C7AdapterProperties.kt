package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7AdapterProperties.Companion.DEFAULT_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = DEFAULT_PREFIX)
class C7AdapterProperties(
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
    const val DEFAULT_PREFIX = "dev.bpm-crafters.process-api.adapter.c7.embedded"
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
    val deliveryStrategy: ExternalServiceTaskDeliveryStrategy,
  )

  /**
   * Strategy how the user tasks are delivered to subscriptions.
   */
  enum class UserTaskDeliveryStrategy {
    /**
     * Delivery by embedded Spring eventing.
     */
    EMBEDDED_EVENT,

    /**
     * Delivery via scheduler.
     */
    EMBEDDED_SCHEDULED,

    /**
     * Delivery via camunda job.
     */
    EMBEDDED_JOB,

    /**
     * Custom delivery.
     */
    CUSTOM
  }


  /**
   * Strategy how the external service tasks are delivered to subscriptions.
   */
  enum class ExternalServiceTaskDeliveryStrategy {
    /**
     * Delivery via scheduler.
     */
    EMBEDDED_SCHEDULED,

    /**
     * Delivery via camunda job.
     */
    EMBEDDED_JOB,

    /**
     * Custom delivery.
     */
    CUSTOM
  }
}
