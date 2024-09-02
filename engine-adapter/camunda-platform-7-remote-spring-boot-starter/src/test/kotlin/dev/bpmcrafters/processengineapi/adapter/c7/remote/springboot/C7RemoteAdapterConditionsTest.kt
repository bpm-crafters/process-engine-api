package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.subscribe.SubscribingClientServiceTaskDelivery
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.c7remote.service-tasks.delivery-strategy = remote_scheduled",
    "dev.bpm-crafters.process-api.adapter.c7remote.user-tasks.delivery-strategy = remote_scheduled"
  ]
)
@ActiveProfiles("itest")
class C7RemoteAdapterScheduledStrategyConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThat(context.getBean(RemotePullServiceTaskDelivery::class.java)).isNotNull()
    assertThat(context.getBean(ServiceTaskCompletionApi::class.java)).isNotNull()
    assertThat(context.getBean(RemotePullUserTaskDelivery::class.java)).isNotNull()
  }

}


@SpringBootTest(
  properties = [
    "camunda.bpm.client.base-url = http://localhost:8080/engine-rest",
    "dev.bpm-crafters.process-api.adapter.c7remote.service-tasks.delivery-strategy = remote_subscribed",
    "dev.bpm-crafters.process-api.adapter.c7remote.user-tasks.delivery-strategy = remote_scheduled"
  ]
)
@ActiveProfiles("itest")
class C7RemoteAdapterSubscribedStrategyConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThat(context.getBean(SubscribingClientServiceTaskDelivery::class.java)).isNotNull()
    assertThat(context.getBean(ServiceTaskCompletionApi::class.java)).isNotNull()
    assertThat(context.getBean(RemotePullUserTaskDelivery::class.java)).isNotNull()
  }

}

@SpringBootTest(
  properties = [
    "dev.bpm-crafters.process-api.adapter.c7remote.service-tasks.delivery-strategy = disabled",
    "dev.bpm-crafters.process-api.adapter.c7remote.user-tasks.delivery-strategy = disabled"
  ]
)
@ActiveProfiles("itest")
class C7RemoteAdapterDisabledConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(RemotePullServiceTaskDelivery::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(ServiceTaskCompletionApi::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(RemotePullUserTaskDelivery::class.java)
    }
  }

}

@SpringBootTest
@ActiveProfiles("withoutProps")
class C7RemoteAdapterWithoutPropsConditionsTest {

  @Autowired
  lateinit var context: ApplicationContext

  @Test
  fun test() {
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(RemotePullServiceTaskDelivery::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(ServiceTaskCompletionApi::class.java)
    }
    assertThrows<NoSuchBeanDefinitionException> {
      context.getBean(RemotePullUserTaskDelivery::class.java)
    }
  }

}

