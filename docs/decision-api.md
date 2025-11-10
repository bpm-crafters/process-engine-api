---
title: Decision Evaluation API
---

The Decision Evaluation API provides functionality to evaluate DMN decision. The API returns a generic 
`DecisionEvaluationResult` which can be cast to single or collect result with the corresponding method.

And here is the example code to evaluate decision:

```java
class DecisionUseCase {

  private final EvaluateDecisionApi evaluateDecisionApi;

  /**
   * Calculates the customer discount. 
   * @param customerStatus customer status.
   * @param year customer registration year.
   * @return calculated discount.
   */
  public Double evaluateDiscount(CustomerStatus customerStatus, Integer year) {
    return (Double)evaluateDecisionApi.evaluateDecision(
      new DecisionByRefEvaluationCommand(
        "customerDiscount",
        () -> Map.of(
          "customerStatus", customerStatus,
          "registrationYear", year 
        ),
        Map.of(CommonRestrictions.TENANT_ID, "myTenant")
      )
    ).get()
      .single()
      .getResult()
      .get("discount")
    ;
  }
}

```
