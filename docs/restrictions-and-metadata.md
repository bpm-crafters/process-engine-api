---
title: Restrictions and metadata
---

Restrictions and metadata use `Map<String, String>` so that the Process Engine API can remain independent of a
particular process engine. The keys defined by `CommonRestrictions` are the portable vocabulary for those maps.

The same key can occur in both places, but the direction matters:

- A **restriction** is an input that narrows or qualifies an API operation. Examples are the `restrictions` of a
  `SubscribeForTaskCmd`, process-start command, correlation command, signal command, or decision-evaluation command.
- **Metadata** is information returned by an adapter. It is available on `TaskInformation.meta` for delivered tasks
  and on `ProcessInformation.meta` after a process start.

An adapter is not required to support or emit every key. Before submitting restrictions, use
`RestrictionAware.getSupportedRestrictions()` on the API in use. A key being supported as a restriction does not
guarantee that the adapter emits it as metadata, and vice versa.

## Restriction keys

| Key | Meaning | Notes |
| --- | --- | --- |
| `ACTIVITY_ID` | BPMN element `id` of an activity. | Use to select work at a specific BPMN activity. This is a definition-level identifier, not an execution identifier. |
| `BUSINESS_KEY` | Logical business identifier of a process instance. | Engines differ in how and where they store the business key. It is not the same as a message correlation key. |
| `CORRELATION_KEY` | Key used to correlate a message with a process instance. | Applies to correlation-oriented operations when supported. |
| `PROCESS_DEFINITION_KEY` | BPMN process-definition key. | A stable, definition-level identifier; distinct from the engine-assigned definition ID. |
| `PROCESS_INSTANCE_ID` | Engine runtime identifier of a process instance. | Use to select a particular running instance. |
| `PROCESS_DEFINITION_ID` | Engine-assigned identifier of a deployed process definition. | Engine-specific and commonly version-specific. |
| `PROCESS_DEFINITION_VERSION_TAG` | User-supplied version tag of a process definition. | Distinct from an engine's numeric version and from the definition ID. |
| `TENANT_ID` | Tenant identifier. | Mutually exclusive with `WITHOUT_TENANT_ID` where both are supported. |
| `WITHOUT_TENANT_ID` | Selects non-tenant-scoped instances or definitions. | Its value is adapter-defined; its presence expresses the restriction. Do not combine it with `TENANT_ID`. |
| `EXECUTION_ID` | Engine runtime execution identifier. | More granular than `PROCESS_INSTANCE_ID`; engine-specific. |
| `WORKER_LOCK_DURATION_IN_MILLISECONDS` | Requested worker lock duration in milliseconds. | An operational delivery setting, not an identity filter. It is meaningful only for adapters and delivery strategies that support worker locks. |

### Message keys without a portable contract yet

`MESSAGE_ID` and `MESSAGE_TTL` are shared constant names, but the API does not currently define a portable value
format, operation scope, or adapter-support contract for them. Applications should not rely on them across adapters
until that contract is specified.

## Result metadata keys

Adapters may add the following `CommonRestrictions` keys to `TaskInformation.meta` and, where meaningful, to
`ProcessInformation.meta`:

| Key | Result contract |
| --- | --- |
| `PROCESS_INSTANCE_ID` | When present in metadata for a task, it is the same value as `ProcessInformation.instanceId` for that process instance. |
| `PROCESS_DEFINITION_KEY` | BPMN process-definition key of the delivered work or started process. |
| `PROCESS_DEFINITION_ID` | Engine-assigned deployed definition identifier. |
| `PROCESS_DEFINITION_VERSION_TAG` | User-supplied definition version tag, if the engine exposes one. |
| `ACTIVITY_ID` | BPMN activity element ID for the delivered task. |
| `ACTIVITY_INSTANCE_ID` | Runtime identifier of that activity occurrence, if exposed by the engine. |
| `EXECUTION_ID` | Engine execution identifier, if exposed by the engine. |
| `BUSINESS_KEY` | Process business key, if the engine exposes it for the task or process. |
| `TENANT_ID` | Tenant identifier; adapters may represent no tenant as an absent or empty value. |

Metadata values are strings. Use `TaskInformation.getMetaValueAsInt()`,
`TaskInformation.getMetaValueAsOffsetDate()`, or `TaskInformation.getMetaValueAsStringSet()` only when the relevant
adapter documents that encoding for its value.

### Task delivery metadata

`TaskInformation` also defines metadata that is not part of `CommonRestrictions`:

| Key | Meaning |
| --- | --- |
| `TaskInformation.REASON` (`reason`) | Delivery or termination reason. The portable values are `create`, `assign`, `update`, `complete`, and `delete`. Its availability depends on the adapter and task event. |
| `TaskInformation.RETRIES` (`retries`) | Remaining retry count, if an adapter provides it. |

## Related identifiers

`SubscribeForTaskCmd.taskDescriptionKey` intentionally is not a `CommonRestrictions` key. It is a separate,
task-specific subscription parameter because adapters use it for engine-specific task descriptors such as a BPMN
implementation, operation implementation reference, or—at minimum—the task XML `id`.

Likewise, a process-start command's `definitionKey` is the primary process selector. Supplying
`PROCESS_DEFINITION_KEY` as an additional restriction does not replace that command field.

## Portable usage

Use `CommonRestrictions.builder()` for the common restriction set and keep engine-specific keys isolated at the
adapter boundary:

```kotlin
val restrictions = CommonRestrictions.builder()
  .withProcessDefinitionKey("order-process")
  .withTenantId("acme")
  .build()

require(taskSubscriptionApi.areSupported(restrictions))
```

For metadata, tolerate absent keys and use `PROCESS_INSTANCE_ID` to relate a delivered task to a process started
through `StartProcessApi`.
