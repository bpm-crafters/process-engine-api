package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AbstractTaskSubscriptionApiImplTest {

  private val subscriptionRepository: SubscriptionRepository = InMemSubscriptionRepository()
  private val taskSubscriptionApi = MyTaskSubscriptionApiImpl(subscriptionRepository)
  class MyTaskSubscriptionApiImpl(
    subscriptionRepository: SubscriptionRepository,
    interceptors: List<TaskHandlerInterceptor> = emptyList()
  ) : AbstractTaskSubscriptionApiImpl(subscriptionRepository, interceptors) {

    override fun meta(instance: MetaInfoAware): MetaInfo {
      TODO("Not yet implemented")
    }
  }

  @Test
  fun `subscribe and unsubscribe`() {

    val x = taskSubscriptionApi.subscribeForTask(
        SubscribeForTaskCmd(
            CommonRestrictions.builder().withProcessDefinitionKey("process-key").build(),
            TaskType.EXTERNAL,
            null,
            setOf("var1"),
            { _, _ -> },
            { _ -> }
        )
    ).get()


    Assertions.assertThat(x).isNotNull
    Assertions.assertThat(x).isInstanceOf(TaskSubscriptionHandle::class.java)
    Assertions.assertThat((x as TaskSubscriptionHandle).taskType).isEqualTo(TaskType.EXTERNAL)
    Assertions.assertThat(this.subscriptionRepository.getTaskSubscriptions()).containsExactly(x)

    taskSubscriptionApi.unsubscribe(
        UnsubscribeFromTaskCmd(x)
    ).get()

    Assertions.assertThat(this.subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }

  @Test
  fun `keeps original action when no interceptors are configured`() {
    val action = TaskHandler { _, _ -> }

    val handle = taskSubscriptionApi.subscribeForTask(
        SubscribeForTaskCmd(
            CommonRestrictions.builder().withProcessDefinitionKey("process-key").build(),
            TaskType.EXTERNAL,
            null,
            setOf("var1"),
            action,
            { _ -> }
        )
    ).get() as TaskSubscriptionHandle

    Assertions.assertThat(handle.action).isSameAs(action)
  }

  @Test
  fun `wraps action so interceptor runs on delivery and unsubscribe still works`() {
    val trace = mutableListOf<String>()
    val interceptor = TaskHandlerInterceptor { _, chain ->
      trace += "before"
      chain.proceed()
      trace += "after"
    }
    val api = MyTaskSubscriptionApiImpl(subscriptionRepository, listOf(interceptor))
    val action = TaskHandler { _, _ -> trace += "action" }

    val handle = api.subscribeForTask(
        SubscribeForTaskCmd(
            CommonRestrictions.builder().withProcessDefinitionKey("process-key").build(),
            TaskType.EXTERNAL,
            null,
            setOf("var1"),
            action,
            { _ -> }
        )
    ).get() as TaskSubscriptionHandle

    Assertions.assertThat(handle.action).isNotSameAs(action)
    handle.action.accept(TaskInformation("task-id", emptyMap()), emptyMap())
    Assertions.assertThat(trace).containsExactly("before", "action", "after")

    api.unsubscribe(UnsubscribeFromTaskCmd(handle)).get()
    Assertions.assertThat(subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }
}
