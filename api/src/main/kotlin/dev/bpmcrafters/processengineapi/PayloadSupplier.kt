package dev.bpmcrafters.processengineapi

import java.util.function.Supplier

/**
 * Supplier for the payload of commands to the engine 
 * Used to for instance define process-variables when starting processes.
 * @since 0.0.1
 */
fun interface PayloadSupplier : Supplier<Map<String, Any?>>
