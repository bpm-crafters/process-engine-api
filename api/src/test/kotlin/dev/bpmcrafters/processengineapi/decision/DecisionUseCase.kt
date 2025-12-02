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
      .single().output as Double
  }

  fun calculateCustomerOffer(customerStatus: CustomerStatus, year: Int): Offer {
    return decisionApi.evaluateDecision(
      DecisionByRefEvaluationCommand(
        decisionRef = "customerOffer",
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
      .many()
      .outputs
      .let { Offer (it["id"] as Integer, it["name"] as String) }
  }

  fun calculateCustomerOffers(customerStatus: CustomerStatus, year: Int): List<Offer> {
    return decisionApi.evaluateDecision(
      DecisionByRefEvaluationCommand(
        decisionRef = "customerOffers",
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
      .collect()
      .result
      .map {
        it.many()
          .outputs
          .let { Offer (it["id"] as Integer, it["name"] as String) }
      }
  }


  enum class CustomerStatus {
    SILVER,
    GOLD,
    PLATINUM
  }

  data class Offer (
      val id: Integer,
      val name: String
    )
}
