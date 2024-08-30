package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.UserTaskDeliveryStrategy
import org.springframework.boot.context.properties.bind.BindResult
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.env.get
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Condition which returns true if `dev.bpm-crafters.process-api.adapter.c7remote.enabled` is true
 */
open class C7RemoteAdapterEnabledCondition : Condition {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    val enabled = context.environment["$DEFAULT_PREFIX.${C7RemoteAdapterProperties::enabled.name}"]
    return enabled != null && enabled.toBoolean()
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.userTasks.deliveryStrategy
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnUserTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnUserTaskDeliveryStrategy(
  val strategy: UserTaskDeliveryStrategy = UserTaskDeliveryStrategy.REMOTE_SCHEDULED,
)

internal class OnUserTaskDeliveryStrategyCondition : C7RemoteAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if(!super.matches(context, metadata)) return false

    val propertiesBindResult: BindResult<C7RemoteAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7RemoteAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val attributes = metadata.getAnnotationAttributes(ConditionalOnUserTaskDeliveryStrategy::class.java.name)
      val strategy = attributes?.get("strategy") as UserTaskDeliveryStrategy

      val properties: C7RemoteAdapterProperties = propertiesBindResult.get()
      return properties.userTasks.deliveryStrategy == strategy
    }

    return false
  }
}

/**
 * Conditions matches if the given strategy is equal to the configured one in application property: `DEFAULT_PREFIX`.serviceTasks.deliveryStrategy
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnServiceTaskDeliveryStrategyCondition::class
)
annotation class ConditionalOnServiceTaskDeliveryStrategy(
  val strategy: ExternalServiceTaskDeliveryStrategy = ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED,
)

internal class OnServiceTaskDeliveryStrategyCondition : C7RemoteAdapterEnabledCondition() {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    if(!super.matches(context, metadata)) return false

    val propertiesBindResult: BindResult<C7RemoteAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7RemoteAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val attributes = metadata.getAnnotationAttributes(ConditionalOnServiceTaskDeliveryStrategy::class.java.name)
      val strategy = attributes?.get("strategy") as ExternalServiceTaskDeliveryStrategy

      val properties: C7RemoteAdapterProperties = propertiesBindResult.get()
      return properties.serviceTasks.deliveryStrategy == strategy
    }

    return false
  }
}
