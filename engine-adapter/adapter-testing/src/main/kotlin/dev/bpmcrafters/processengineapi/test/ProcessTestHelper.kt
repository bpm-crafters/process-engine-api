package dev.bpmcrafters.processengineapi.test

import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi

interface ProcessTestHelper {

  fun getStartProcessApi(): StartProcessApi

  fun getTaskSubscriptionApi(): TaskSubscriptionApi

  fun getUserTaskCompletionApi(): UserTaskCompletionApi

  fun getServiceTaskCompletionApi(): ServiceTaskCompletionApi

  fun triggerPullingUserTaskDeliveryManually()

  fun subscribeForUserTasks()

  fun triggerExternalTaskDeliveryManually()

  fun getProcessInformation(instanceId: String): ProcessInformation

  fun clearAllSubscriptions()

}
