package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion

class LinearMemoryFailureRetrySupplier(
  private val retry: Int,
  private val retryTimeout: Long
) : FailureRetrySupplier {

  private val taskFailures: MutableMap<String, FailureRetrySupplier.FailureRetry> = mutableMapOf()

  override fun apply(taskId: String): FailureRetrySupplier.FailureRetry {
    val last = taskFailures.getOrPut(taskId) { FailureRetrySupplier.FailureRetry(retryCount = retry, retryTimeout = retryTimeout) }
    val new = last.copy(retryCount = last.retryCount - 1)
    taskFailures[taskId] = new
    return new
  }
}
