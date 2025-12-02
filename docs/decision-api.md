---
title: Decision Evaluation API
---

The Decision Evaluation API provides functionality to evaluate a DMN decision. The API returns a generic
`DecisionEvaluationResult` which can be cast to a single or collect result using the corresponding method:

- `single()` → returns `SingleDecisionEvaluationResult`
- `collect()` → returns `CollectDecisionEvaluationResult`

Each of those wraps a `DecisionEvaluationOutput` value which can be either:

- `DecisionEvaluationSingleOutput` for context-less single values (access via `single().getOutput()`), or
- `DecisionEvaluationMultiOutput` for named/context values (access via `many().getOutputs()`).

Below are example snippets in Java showing the new API usage.

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
    return (Double) evaluateDecisionApi.evaluateDecision(
      new DecisionByRefEvaluationCommand(
        "customerDiscount",
        () -> Map.of(
          "customerStatus", customerStatus,
          "registrationYear", year 
        ),
        Map.of(CommonRestrictions.TENANT_ID, "myTenant")
      )
    ).get()                 // DecisionEvaluationResult
     .single()              // SingleDecisionEvaluationResult
     .getResult()           // DecisionEvaluationOutput
     .single()              // DecisionEvaluationSingleOutput
     .getOutput();          // actual value, e.g. Double
  }

  /**
   * Calculates a customer offer with multiple named outputs (multi-output decision).
   */
  public Offer evaluateOffer(CustomerStatus customerStatus, Integer year) {
    Map<String, Object> outputs = evaluateDecisionApi.evaluateDecision(
      new DecisionByRefEvaluationCommand(
        
        "customerOffer",
        () -> Map.of(
          "customerStatus", customerStatus,
          "registrationYear", year
        ),
        Map.of(CommonRestrictions.TENANT_ID, "myTenant")
      )
    ).get()                 // DecisionEvaluationResult
     .single()              // SingleDecisionEvaluationResult
     .getResult()           // DecisionEvaluationOutput
     .many()                // DecisionEvaluationMultiOutput
     .getOutputs();         // Map<String, Object>

    return new Offer((Integer) outputs.get("id"), (String) outputs.get("name"));
  }

  /**
   * Calculates multiple customer offers (collect decision result).
   */
  public List<Offer> evaluateOffers(CustomerStatus customerStatus, Integer year) {
    return evaluateDecisionApi.evaluateDecision(
      new DecisionByRefEvaluationCommand(
        "customerOffers",
        () -> Map.of(
          "customerStatus", customerStatus,
          "registrationYear", year
        ),
        Map.of(CommonRestrictions.TENANT_ID, "myTenant")
      )
    ).get()                 // DecisionEvaluationResult
     .collect()             // CollectDecisionEvaluationResult
     .getResult()           // List<DecisionEvaluationOutput>
     .stream()
     .map(o -> {
       Map<String, Object> outputs = o.many().getOutputs();
       return new Offer((Integer) outputs.get("id"), (String) outputs.get("name"));
     })
     .toList();
  }
}

```
