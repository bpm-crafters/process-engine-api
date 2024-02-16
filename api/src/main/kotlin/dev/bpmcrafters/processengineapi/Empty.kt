package dev.bpmcrafters.processengineapi

/**
 * Special empty marker, to be able to deliver the Void / Unit, independent of Java / Kotlin.
 */
object Empty {
  /**
   * Comply with hashcode contract in Java.
   */
  override fun hashCode(): Int = 1983210398
}
