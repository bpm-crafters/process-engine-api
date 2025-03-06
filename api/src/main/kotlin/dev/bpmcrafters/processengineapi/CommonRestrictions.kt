package dev.bpmcrafters.processengineapi

/**
 * Helper to build restrictions for subscriptions or correlations.
 * @since 0.0.1
 */
object CommonRestrictions {

  /**
   * Definition attribute of a BPMN activity from XML holding the id of the element.
   */
  const val ACTIVITY_ID = "activityId"

  /**
   * Logical business key.
   */
  const val BUSINESS_KEY = "businessKey"

  /**
   * Special key for correlation with processes.
   */
  const val CORRELATION_KEY = "correlationKey"

  /**
   * Definition attribute of a BPMN process from XML holding the id of the element.
   */
  const val PROCESS_DEFINITION_KEY = "processDefinitionKey"

  /**
   * Id provided by the runtime identifying the process instance.
   */
  const val PROCESS_INSTANCE_ID = "processInstanceId"

  /**
   * Id provided by the runtime to identify a deployed process definition.
   */
  const val PROCESS_DEFINITION_ID = "processDefinitionId"

  /**
   * Process definition version tag provided by the user.
   */
  const val PROCESS_DEFINITION_VERSION_TAG = "processDefinitionVersionTag"

  /**
   * Definition key of a task.
   */
  @Deprecated(
    message = "Replaced by ACTIVITY_ID, since it a more generic term.",
    replaceWith = ReplaceWith("ACTIVITY_ID"),
    level = DeprecationLevel.WARNING
  )
  const val TASK_DEFINITION_KEY = "taskDefinitionKey"

  /**
   * Tenant id.
   */
  const val TENANT_ID = "tenantId"

  /**
   * Indicating the absence of tenant.
   */
  const val WITHOUT_TENANT_ID = "withoutTenantId"
  const val MESSAGE_ID = "messageId"
  const val MESSAGE_TTL = "messageTTL"

  /**
   * Id of execution in runtime.
   */
  const val EXECUTION_ID = "executionId"

  /**
   * Creates a helper restrictions builder.
   */
  @JvmStatic
  fun builder(): RestrictionsBuilder = RestrictionsBuilder()

  class RestrictionsBuilder internal constructor() {

    private val restrictions = mutableMapOf<String, String>()

    fun withProcessDefinitionKey(processDefinitionKey: String) = this.apply {
      restrictions[PROCESS_DEFINITION_KEY] = processDefinitionKey
    }

    @Deprecated(
      message = "Replaced by withActivityId, since it a more generic term.",
      replaceWith = ReplaceWith("withActivityId")
    )
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
