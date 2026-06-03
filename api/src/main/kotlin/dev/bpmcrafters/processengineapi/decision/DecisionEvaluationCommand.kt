package dev.bpmcrafters.processengineapi.decision

import dev.bpmcrafters.processengineapi.ExecutionModeAware
import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Interface for decision evaluation commands.
 * @since 1.4
 */
interface DecisionEvaluationCommand : PayloadSupplier, ExecutionModeAware
