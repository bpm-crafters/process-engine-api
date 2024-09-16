package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import java.util.function.Function

@FunctionalInterface
interface FailureRetrySupplier : Function<String, FailureRetrySupplier.FailureRetry> {

  data class FailureRetry(
    val retryCount: Int,
    val retryTimeout: Long
  )
}
