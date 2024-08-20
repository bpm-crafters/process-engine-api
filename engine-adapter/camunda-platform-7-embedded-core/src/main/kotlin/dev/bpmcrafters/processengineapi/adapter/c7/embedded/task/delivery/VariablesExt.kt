package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle

fun Map<String, Any>.filterBySubscription(subscription: TaskSubscriptionHandle): Map<String, Any> = if (subscription.payloadDescription == null) {
  this
} else {
  if (subscription.payloadDescription!!.isEmpty()) {
    mapOf()
  } else {
    this.filterKeys { key -> subscription.payloadDescription!!.contains(key) }
  }
}
