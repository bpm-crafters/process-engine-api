package dev.bpmcrafters.processengineapi

import java.util.function.Supplier

/**
 * Supplier for the payload.
 * @since 0.0.1
 */
fun interface PayloadSupplier : Supplier<Map<String, Any>>
