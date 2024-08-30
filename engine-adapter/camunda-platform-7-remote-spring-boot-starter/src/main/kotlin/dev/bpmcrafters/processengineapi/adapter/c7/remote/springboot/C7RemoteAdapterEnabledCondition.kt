package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import org.springframework.boot.context.properties.bind.BindResult
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Condition which returns true if `dev.bpm-crafters.process-api.adapter.c7remote.enabled` is true
 */
class C7RemoteAdapterEnabledCondition : Condition {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    val propertiesBindResult: BindResult<C7RemoteAdapterProperties> = Binder.get(context.environment)
      .bind(C7RemoteAdapterProperties.DEFAULT_PREFIX, C7RemoteAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: C7RemoteAdapterProperties = propertiesBindResult.get()
      return properties.enabled
    }

    return false
  }
}
