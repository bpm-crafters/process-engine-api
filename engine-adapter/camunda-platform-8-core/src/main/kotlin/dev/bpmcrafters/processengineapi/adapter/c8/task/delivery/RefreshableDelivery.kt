package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

/**
 * Refreshable delivery.
 * @since 0.1.0
 */
interface RefreshableDelivery {
  /**
   * Triggers a refresh in the delivery.
   */
  fun refresh()
}
