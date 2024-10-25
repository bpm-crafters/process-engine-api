package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.c7embedded.service-tasks.delivery-strategy = embedded_scheduled",
    "dev.bpm-crafters.process-api.adapter.c7embedded.user-tasks.delivery-strategy = embedded_scheduled"
  ]
)
@ActiveProfiles("itest")
class C7EmbeddedAdapterScheduledStrategyConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThat(context.getBean(EmbeddedPullServiceTaskDelivery::class.java)).isNotNull()
    assertThat(context.getBean(ServiceTaskCompletionApi::class.java)).isNotNull()
    assertThat(context.getBean(EmbeddedPullUserTaskDelivery::class.java)).isNotNull()
  }

}


@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.c7embedded.service-tasks.delivery-strategy = disabled",
    "dev.bpm-crafters.process-api.adapter.c7embedded.user-tasks.delivery-strategy = disabled"
  ]
)
@ActiveProfiles("itest")
class C7EmbeddedAdapterDisabledConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(EmbeddedPullServiceTaskDelivery::class.java)
    }
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(EmbeddedPullUserTaskDelivery::class.java)
    }
  }

}

@SpringBootTest
@ActiveProfiles("withoutProps")
class C7EmbeddedAdapterWithoutPropsConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(EmbeddedPullServiceTaskDelivery::class.java)
    }
    org.junit.jupiter.api.assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(EmbeddedPullUserTaskDelivery::class.java)
    }
  }

}

