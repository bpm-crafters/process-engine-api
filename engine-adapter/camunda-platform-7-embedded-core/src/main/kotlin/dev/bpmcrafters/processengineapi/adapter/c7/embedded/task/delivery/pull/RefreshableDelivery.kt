package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull

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
