# Process application lifecycle

The process application lifecycle provides an engine-independent, ordered sequence of application states. It gives adapters and application components a common place to perform startup work, register initial task subscriptions, begin normal work, and release resources during shutdown.

The lifecycle contract is `ProcessApplicationLifecycle`. Its default coordinator is `DefaultProcessApplicationLifecycle`. Neither type depends on Spring Boot or a particular process engine.

## Phases

A phase is an `Int`, not an enum. The named constants in `ProcessApplicationPhase` are stable boundaries; applications may use any integer between two boundaries for their own ordered participants.

| Phase | Value | Meaning | Typical operation |
| --- | ---: | --- | --- |
| `NOT_STARTED` | `Int.MIN_VALUE` | Initial state before startup. | Register initial lifecycle participants. |
| `INFRASTRUCTURE_READY` | `Int.MIN_VALUE + 1000` | Process Engine API infrastructure is available. | Validate adapter infrastructure or prepare application resources. |
| `SUBSCRIPTION_REGISTRATION` | `Int.MIN_VALUE + 10000` | Synchronous initial task-subscription window. | Register initial task subscriptions using `TaskSubscriptionRegistrationContext`. |
| `SUBSCRIPTIONS_REGISTERED` | `Int.MIN_VALUE + 20000` | Initial subscription registration has completed. | Start components that rely on all initial subscriptions existing. |
| `RUNNING` | `42` | Normal application delivery may begin. | Start consumers, scheduled work, or application-facing services. |
| `STOPPING` | `Int.MAX_VALUE - 1000` | New engine work must not start. | Stop consumers and drain or reject new work. |
| `STOPPED` | `Int.MAX_VALUE` | Lifecycle resources have been released. | Perform final cleanup or record that the application stopped. |

For example, a participant at `SUBSCRIPTIONS_REGISTERED + 10` executes after the initial subscriptions have been registered and before `RUNNING`. A participant at `RUNNING + 10` executes after normal delivery begins and before shutdown starts.

## Registering participants

Implement `ProcessApplicationLifecycleParticipant` with a phase and an `onPhase` callback. Participants execute synchronously in ascending numeric phase order while the lifecycle coordinator monitor is held. Do not wait for a thread that calls lifecycle registration or transition methods from a participant, because that can deadlock.

```kotlin
class StopConsumerParticipant(
  private val consumer: MessageConsumer
) : ProcessApplicationLifecycleParticipant {

  override fun getPhase(): Int = ProcessApplicationPhase.STOPPING

  override fun onPhase(context: ProcessApplicationLifecycleContext) {
    consumer.stop()
  }
}

lifecycle.register(StopConsumerParticipant(consumer))
```

A participant can be registered while the application is running when its phase is still in the future. For example, registering a `STOPPING` participant during `RUNNING` is supported. Registration for an already reached phase, or while a transition is currently dispatching participants, is rejected because that participant could otherwise miss its callback.

Registering a lifecycle participant is distinct from registering a task subscription. `TaskSubscriptionApi` remains usable for dynamic subscriptions during normal application runtime; `SUBSCRIPTION_REGISTRATION` is the ordered window for initial registrations.

## Driving transitions

Transitions are monotonic and idempotent. Requesting a phase at or below the reached phase does nothing. Requesting a later phase drives each missing named boundary in order, including all participants placed between those boundaries.

```kotlin
lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
```

Crossing `SUBSCRIPTION_REGISTRATION` needs a non-null `TaskSubscriptionRegistrationContext`, because participants at that boundary need the `TaskSubscriptionApi` used for initial registration.

```kotlin
lifecycle.transitionTo(
  ProcessApplicationPhase.RUNNING,
  TaskSubscriptionRegistrationContext(taskSubscriptionApi)
)
```

The example reaches, in order, `INFRASTRUCTURE_READY`, `SUBSCRIPTION_REGISTRATION`, `SUBSCRIPTIONS_REGISTERED`, and `RUNNING`. Calling the one-argument overload for that same transition before registration was reached fails with an error such as:

```text
Lifecycle transition to phase RUNNING (42) requires a TaskSubscriptionRegistrationContext
```

After `SUBSCRIPTION_REGISTRATION` has been reached, later transitions no longer need that context:

```kotlin
lifecycle.transitionTo(ProcessApplicationPhase.STOPPING)
lifecycle.transitionTo(ProcessApplicationPhase.STOPPED)
```

Every entered phase is logged at `INFO` level before its participants run. A second message is logged after the phase is reached.

```text
PROCESS-ENGINE-API-LIFECYCLE-001: Entering lifecycle phase RUNNING (42).
PROCESS-ENGINE-API-LIFECYCLE-002: Lifecycle phase RUNNING (42) reached.
```

If a startup participant fails, `PROCESS-ENGINE-API-LIFECYCLE-003` is logged and the lifecycle becomes unusable, preventing retries that could repeat partial startup work. During `STOPPING` and `STOPPED`, each participant is still invoked after an individual failure; the lifecycle reaches the requested shutdown phase and then throws an aggregate exception containing all participant failures.
