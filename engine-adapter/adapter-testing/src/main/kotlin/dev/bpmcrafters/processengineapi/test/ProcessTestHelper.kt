package dev.bpmcrafters.processengineapi.test

import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi

interface ProcessTestHelper {

  fun getStartProcessApi(): StartProcessApi

  fun getTaskSubscriptionApi(): TaskSubscriptionApi

  fun getUserTaskCompletionApi(): UserTaskCompletionApi

  fun getExternalTaskCompletionApi(): ExternalTaskCompletionApi

  fun triggerUserTaskDeliveryManually()

  fun triggerExternalTaskDeliveryManually()

  fun getProcessInformation(instanceId: String): ProcessInformation

  fun clearAllSubscriptions()

}
