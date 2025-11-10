package dev.bpmcrafters.processengineapi.decision

import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.RestrictionAware
import java.util.concurrent.CompletableFuture

/**
 * Evaluate Decision API.
 * @since 2.0
 */
interface EvaluateDecisionApi : RestrictionAware, MetaInfoAware {

  /**
   * Evaluate decision.
   * @param command a command containing parameter for decision evaluation.
   * @return decision evaluation result. Depending on the hit policy might either [SingleDecisionEvaluationResult]
   * or [CollectDecisionEvaluationResult].
   */
  fun evaluateDecision(command: DecisionEvaluationCommand): CompletableFuture<DecisionEvaluationResult>
}
