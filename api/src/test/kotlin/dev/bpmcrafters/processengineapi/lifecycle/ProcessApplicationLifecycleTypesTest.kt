package dev.bpmcrafters.processengineapi.lifecycle

import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class ProcessApplicationLifecycleTypesTest {

  @Test
  fun `returns names for all named phases`() {
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.NOT_STARTED)).isEqualTo("NOT_STARTED")
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.INFRASTRUCTURE_READY)).isEqualTo("INFRASTRUCTURE_READY")
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION)).isEqualTo("SUBSCRIPTION_REGISTRATION")
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED)).isEqualTo("SUBSCRIPTIONS_REGISTERED")
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.RUNNING)).isEqualTo("RUNNING")
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.STOPPING)).isEqualTo("STOPPING")
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.STOPPED)).isEqualTo("STOPPED")
  }

  @Test
  fun `returns no name for a custom phase`() {
    assertThat(ProcessApplicationPhase.getName(ProcessApplicationPhase.RUNNING + 1)).isNull()
  }

  @Test
  fun `provides its phase through lifecycle context`() {
    val context = ProcessApplicationLifecycleContext(99)

    assertThat(context.getPhase()).isEqualTo(99)
  }

  @Test
  fun `provides task subscription API through registration context`() {
    val taskSubscriptionApi = mock<TaskSubscriptionApi>()
    val context = TaskSubscriptionRegistrationContext(taskSubscriptionApi)

    assertThat(context.getPhase()).isEqualTo(ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION)
    assertThat(context.getTaskSubscriptionApi()).isSameAs(taskSubscriptionApi)
  }
}
