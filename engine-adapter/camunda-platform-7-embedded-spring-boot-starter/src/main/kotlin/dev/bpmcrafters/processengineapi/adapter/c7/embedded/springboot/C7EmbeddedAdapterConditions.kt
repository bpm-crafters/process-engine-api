package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.*
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import org.springframework.boot.context.properties.bind.BindResult
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata


/**
 * Condition which returns true if `dev.bpm-crafters.process-api.adapter.c7embedded.enabled` is true
 */
open class C7EmbeddedAdapterEnabledCondition : Condition {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    // bind the value of "enabled" property
    val booleanBinderResult = Binder.get(context.environment)
      .bind("$DEFAULT_PREFIX.${C7EmbeddedAdapterProperties::enabled.name}", Boolean::class.java)
    if (booleanBinderResult.isBound) {
      return booleanBinderResult.get()
    }
    return false
  }
}

/**
 * Condition which returns true if the following conditions are true:
 * * `dev.bpm-crafters.process-api.adapter.c7embedded.enabled` is true
 * * `dev.bpm-crafters.process-api.adapter.c7embedded.user-tasks.execute-initial-pull-on-startup` is true
 */
open class C7EmbeddedAdapterUserTaskInitialPullEnabledCondition : C7EmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<C7EmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7EmbeddedAdapterProperties::class.java)
    if (propertiesBindResult.isBound) {
      return propertiesBindResult.get().userTasks.executeInitialPullOnStartup
    }
    return false
  }
}

/**
 * Condition which returns true if the following conditions are true:
 * * `dev.bpm-crafters.process-api.adapter.c7embedded.enabled` is true
 * * `dev.bpm-crafters.process-api.adapter.c7embedded.service-tasks.execute-initial-pull-on-startup` is true
 */
open class C7EmbeddedAdapterServiceTaskInitialPullEnabledCondition : C7EmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<C7EmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7EmbeddedAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      return propertiesBindResult.get().serviceTasks.executeInitialPullOnStartup
    }
    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.userTasks.deliveryStrategy
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnUserTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnUserTaskDeliveryStrategy(
  val strategies: Array<UserTaskDeliveryStrategy> = [UserTaskDeliveryStrategy.EMBEDDED_SCHEDULED],
)

internal class OnUserTaskDeliveryStrategyCondition : C7EmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {

    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<C7EmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7EmbeddedAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: C7EmbeddedAdapterProperties = propertiesBindResult.get()

      @Suppress("UNCHECKED_CAST")
      val strategies: Array<UserTaskDeliveryStrategy> = metadata
        .getAnnotationAttributes(ConditionalOnUserTaskDeliveryStrategy::class.java.name)
        ?.get(ConditionalOnUserTaskDeliveryStrategy::strategies.name) as Array<UserTaskDeliveryStrategy>

      return strategies.contains(properties.userTasks.deliveryStrategy)
    }

    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.serviceTasks.deliveryStrategy
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnServiceTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnServiceTaskDeliveryStrategy(
  val strategy: ExternalServiceTaskDeliveryStrategy = ExternalServiceTaskDeliveryStrategy.EMBEDDED_SCHEDULED,
)

internal class OnServiceTaskDeliveryStrategyCondition : C7EmbeddedAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if (!super.matches(context, metadata)) {
      return false
    }

    val propertiesBindResult: BindResult<C7EmbeddedAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7EmbeddedAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: C7EmbeddedAdapterProperties = propertiesBindResult.get()

      val strategy = metadata
        .getAnnotationAttributes(ConditionalOnServiceTaskDeliveryStrategy::class.java.name)
        ?.get(ConditionalOnServiceTaskDeliveryStrategy::strategy.name) as ExternalServiceTaskDeliveryStrategy

      return properties.serviceTasks.deliveryStrategy == strategy
    }

    return false
  }
}
