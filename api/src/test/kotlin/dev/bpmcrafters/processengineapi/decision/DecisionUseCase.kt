package dev.bpmcrafters.processengineapi.decision

import dev.bpmcrafters.processengineapi.CommonRestrictions

/**
 * Example use cae to demonstrate the usage of the API.
 * @since 1.4
 */
internal class DecisionUseCase(
  private val decisionApi: EvaluateDecisionApi
) {

  fun calculateCustomerDiscount(customerStatus: CustomerStatus, year: Int): Double {
    return decisionApi.evaluateDecision(
      DecisionByRefEvaluationCommand(
        decisionRef = "customerDiscount",
        payload = mapOf(
          "customerStatus" to customerStatus,
          "year" to year
        ),
        restrictions = mapOf(
          CommonRestrictions.TENANT_ID to "tenant-1"
        )
      )
    ).get()
      .asSingle()
      ?.asType<Double>()
      ?: NO_DISCOUNT
  }

  fun calculateCustomerOffer(customerStatus: CustomerStatus, year: Int): Offer {
    return decisionApi.evaluateDecision(
      DecisionByRefEvaluationCommand(
        decisionRef = "customerOffer",
        payload = mapOf(
          "customerStatus" to customerStatus,
          "year" to year
        ),
        restrictions = mapOf(
          CommonRestrictions.TENANT_ID to "tenant-1"
        )
      )
    ).get()
      .asSingle()
      ?.asMap()
      ?.let { Offer(it["id"] as Integer, it["name"] as String) }
      ?: throw IllegalStateException("No offer found")
  }

  fun calculateCustomerOffers(customerStatus: CustomerStatus, year: Int): List<Offer> {
    return decisionApi.evaluateDecision(
      DecisionByRefEvaluationCommand(
        decisionRef = "customerOffers",
        payload = mapOf(
          "customerStatus" to customerStatus,
          "year" to year
        ),
        restrictions =
          mapOf(
            CommonRestrictions.TENANT_ID to "tenant-1"
          )
      )
    ).get()
      .asList()
      .map { result -> result
        .asMap()
        .let { Offer(it["id"] as Integer, it["name"] as String) } }
  }

  enum class CustomerStatus {
    SILVER,
    GOLD,
    PLATINUM
  }

  data class Offer(
    val id: Integer,
    val name: String
  )

  companion object {
    const val NO_DISCOUNT = 0.0
  }
}
