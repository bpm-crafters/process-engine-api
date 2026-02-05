package dev.bpmcrafters.processengineapi.impl.task

/**
 * Filter the payload by the requested payload description of the subscription.
 * @param subscription with payload description (the value may be null).
 * @return filtered payload variables.
 *
 * @since 0.0.1
 */
fun Map<String, Any?>.filterBySubscription(subscription: TaskSubscriptionHandle): Map<String, Any?> =
  if (subscription.payloadDescription != null) {
    if (subscription.payloadDescription.isEmpty()) {
      mapOf()
    } else {
      this.filterKeys { key -> subscription.payloadDescription.contains(key) }
    }
  } else {
    this
  }
