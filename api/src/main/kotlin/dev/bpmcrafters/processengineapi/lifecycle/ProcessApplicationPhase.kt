package dev.bpmcrafters.processengineapi.lifecycle

/**
 * Named integer boundaries in the process application lifecycle.
 *
 * Applications can register participants at any integer strictly between two named boundaries.
 *
 * @since 1.8.0
 */
class ProcessApplicationPhase private constructor() {

  companion object {
    /**
     * Lifecycle has been created but startup has not begun.
     */
    const val NOT_STARTED: Int = Int.MIN_VALUE

    /**
     * Process Engine API infrastructure is available.
     */
    const val INFRASTRUCTURE_READY: Int = Int.MIN_VALUE + 1000

    /**
     * Application components register their task subscriptions synchronously.
     */
    const val SUBSCRIPTION_REGISTRATION: Int = Int.MIN_VALUE + 10000

    /**
     * The one-time subscription-registration window has closed.
     */
    const val SUBSCRIPTIONS_REGISTERED: Int = Int.MIN_VALUE + 20000

    /**
     * Startup participants have completed and normal delivery may begin.
     */
    const val RUNNING: Int = 42

    /**
     * New engine interaction must not begin.
     */
    const val STOPPING: Int = Int.MAX_VALUE - 1000

    /**
     * Lifecycle resources have been released.
     */
    const val STOPPED: Int = Int.MAX_VALUE

    /**
     * Returns the symbolic name of a named lifecycle boundary, or `null` for a custom phase.
     */
    @JvmStatic
    fun getName(phase: Int): String? = when (phase) {
      NOT_STARTED -> "NOT_STARTED"
      INFRASTRUCTURE_READY -> "INFRASTRUCTURE_READY"
      SUBSCRIPTION_REGISTRATION -> "SUBSCRIPTION_REGISTRATION"
      SUBSCRIPTIONS_REGISTERED -> "SUBSCRIPTIONS_REGISTERED"
      RUNNING -> "RUNNING"
      STOPPING -> "STOPPING"
      STOPPED -> "STOPPED"
      else -> null
    }
  }
}
