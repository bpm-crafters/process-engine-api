package dev.bpmcrafters.processengineapi.decision

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.PayloadSupplier
import java.util.function.Supplier

internal class DecisionUseCase(
  private val decisionApi: EvaluateDecisionApi
) {

  fun calculateCustomerDiscount(customerStatus: CustomerStatus, year: Int): Double {
    return decisionApi.evaluateDecision(
      DecisionByRefEvaluationCommand(
        decisionRef = "customerDiscount",
        payloadSupplier = PayloadSupplier {
          mapOf<String, Any>(
            "customerStatus" to customerStatus,
            "year" to year
          )
        },
        restrictionSupplier = Supplier {
          mapOf<String, String>(CommonRestrictions.TENANT_ID to "tenant-1")
        }
      )
    ).get()
      .single()
      .result
      .values["discount"] as Double
  }

  enum class CustomerStatus {
    SILVER,
    GOLD,
    PLATINUM
  }
}
