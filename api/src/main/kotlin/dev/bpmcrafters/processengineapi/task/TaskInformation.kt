package dev.bpmcrafters.processengineapi.task

import java.time.OffsetDateTime

/**
 * Represents task information.
 * @since 0.0.1
 */
data class TaskInformation(
  /**
   * Reference to the instance.
   */
  val taskId: String,
  /**
   * Additional metadata about the task.
   */
  val meta: Map<String, String>
) {
  companion object {
    const val REASON = "reason"
    const val RETRIES = "retries"

    const val CREATE = "create"
    const val ASSIGN = "assign"
    const val UPDATE = "update"
    const val COMPLETE = "complete"
    const val DELETE = "delete"
  }

  /**
   * Creates a new task information with specified reason.
   * @param reason reason to set.
   * @return task information.
   */
  fun withReason(reason: String): TaskInformation {
    return this.copy(meta = meta + (REASON to reason))
  }

  /**
   * Cleans up th reason, if previously available.
   * @return task information without reason set.
   */
  fun cleanupReason(): TaskInformation {
    return this.copy(meta = meta.filterNot { it.key == REASON })
  }

  /**
   * Returns a value of the meta referenced by the key as OffsetDateTime.
   * @param key key of the meta value.
   * @return value as `OffsetDateTime` or `null`.
   */
  fun getMetaValueAsOffsetDate(key: String): OffsetDateTime? {
    val value = meta[key]
    return if (value.isNullOrBlank()) {
      null
    } else {
      OffsetDateTime.parse(value)
    }
  }

  /**
   * Returns a value of meta referenced by the key as Set<String>.
   * @param key key of the meta value.
   * @return value as `Set<String>` or `null`.
   */
  fun getMetaValueAsStringSet(key: String): Set<String>? {
    val value = meta[key]
    return if (value.isNullOrBlank()) {
      null
    } else {
      value.split(",").toSet()
    }
  }

  /**
   * Returns a value of the meta referenced by the key as Int.
   * @param key key of the meta value.
   * @return value as `Int` or `null`.
   */
  fun getMetaValueAsInt(key: String): Int? {
    return meta[key]?.toInt()
  }

}
