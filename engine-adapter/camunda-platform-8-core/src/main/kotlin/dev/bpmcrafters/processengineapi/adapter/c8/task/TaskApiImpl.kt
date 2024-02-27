package dev.bpmcrafters.processengineapi.adapter.c8.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.*
import io.camunda.zeebe.client.ZeebeClient
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class TaskApiImpl(
  private val subscriptionRepository: SubscriptionRepository,
  private val zeebeClient: ZeebeClient
) : TaskApi {

  companion object : KLogging()

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription> {
    return TaskSubscriptionHandle(
      taskDescriptionKey = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      restrictions = cmd.restrictions,
      action = cmd.action,
      modification = cmd.modifcation
    ).let {
      subscriptionRepository.createTaskSubscription(it)
      logger.info { "Registered new task subscription $it" }
      CompletableFuture.completedFuture(it)
    }
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Empty> {
    subscriptionRepository.deleteTaskSubscription(ensure(cmd.subscription))
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Empty> {
    zeebeClient
      .newCompleteCommand(cmd.taskId.toLong())
      .variables(cmd.get())
      .send()
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      modification.terminated(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Empty> {
    zeebeClient
      .newThrowErrorCommand(cmd.taskId.toLong())
      .errorCode(cmd.error)
      .variables(cmd.get())
      .send()
    subscriptionRepository.removeSubscriptionForTask(cmd.taskId)?.apply {
      modification.terminated(cmd.taskId)
    }
    return CompletableFuture.completedFuture(Empty)
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  override fun getSupportedRestrictions(): Set<String> = setOf()

  private fun ensure(subscription: TaskSubscription): TaskSubscriptionHandle {
    require(subscription is TaskSubscriptionHandle) {
      "Only subscriptions of type ${TaskSubscriptionHandle::class.java.name} are supported, but got ${subscription.javaClass.name}."
    }
    return subscription
  }

}
