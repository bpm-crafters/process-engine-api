package dev.bpmcrafters.processengineapi

/**
 * Helper to build restrictions for subscriptions or correlations.
 * @since 0.0.1
 */
object CommonRestrictions {

  const val ACTIVITY_ID = "activityId"
  const val BUSINESS_KEY = "businessKey"
  const val CORRELATION_KEY = "correlationKey"
  const val PROCESS_DEFINITION_KEY = "processDefinitionKey"
  const val PROCESS_INSTANCE_ID = "processInstanceId"
  const val TASK_TYPE = "taskType"
  const val TASK_DEFINITION_KEY = "taskDefinitionKey"
  const val TENANT_ID = "tenantId"
  const val MESSAGE_ID = "messageId"
  const val MESSAGE_TTL = "messageTTL"
  const val EXECUTION_ID = "executionId"

  const val TASK_TYPE_SERVICE = "service"
  const val TASK_TYPE_USER = "user"

  /**
   * Creates a helper restrictions builder.
   */
  @JvmStatic
  fun builder(): RestrictionsBuilder = RestrictionsBuilder()

  class RestrictionsBuilder internal constructor() {

    private val restrictions = mutableMapOf<String, String>()

    fun withTaskType(taskType: String) = this.apply {
      restrictions[TASK_TYPE] = taskType
    }

    fun serviceTasks() = this.apply {
      restrictions[TASK_TYPE] = TASK_TYPE_SERVICE
    }

    fun userTasks() = this.apply {
      restrictions[TASK_TYPE] = TASK_TYPE_USER
    }

    fun withProcessDefinitionKey(processDefinitionKey: String) = this.apply {
      restrictions[PROCESS_DEFINITION_KEY] = processDefinitionKey
    }

    fun withTaskDefinitionKey(taskDefinitionKey: String) = this.apply {
      restrictions[TASK_DEFINITION_KEY] = taskDefinitionKey
    }

    fun withTenantId(tenantId: String) = this.apply {
      restrictions[TENANT_ID] = tenantId
    }

    fun withInstanceId(instanceId: String) = this.apply {
      restrictions[PROCESS_INSTANCE_ID] = instanceId
    }

    fun withActivityId(activityId: String) = this.apply {
      restrictions[ACTIVITY_ID] = activityId
    }

    fun withBusinessKey(businessKey: String) = this.apply {
      restrictions[BUSINESS_KEY] = businessKey
    }

    fun withCorrelationKey(correlationKey: String) = this.apply {
      restrictions[CORRELATION_KEY] = correlationKey
    }

    fun withRestriction(restriction: String, value: String) = this.apply {
      restrictions[restriction] = value
    }

    fun build(): Map<String, String> = restrictions.toMap()
  }
}
