package dev.bpmcrafters.processengineapi.correlation

import java.util.function.Supplier

/**
 * Correlation supplier.
 * @since 0.0.1
 */
interface CorrelationSupplier : Supplier<Correlation>
