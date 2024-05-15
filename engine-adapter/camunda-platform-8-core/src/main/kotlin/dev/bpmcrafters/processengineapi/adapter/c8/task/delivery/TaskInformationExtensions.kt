package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import io.camunda.tasklist.dto.Task
import io.camunda.zeebe.client.api.response.ActivatedJob

fun ActivatedJob.toTaskInformation(): TaskInformation = TaskInformation(
  taskId = "${this.key}",
  meta = mapOf(
    CommonRestrictions.TENANT_ID to this.tenantId,
    CommonRestrictions.TASK_DEFINITION_KEY to this.elementId,
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.bpmnProcessId,
    CommonRestrictions.PROCESS_DEFINITION_ID to "${this.processDefinitionKey}",
    CommonRestrictions.PROCESS_INSTANCE_ID to "${this.processInstanceKey}",
    "formKey" to this.customHeaders.getOrDefault("io.camunda.zeebe:formKey", null),
    // FIXME more from the job.customHeaders!
  )
)

fun Task.toTaskInformation(): TaskInformation = TaskInformation(
  taskId = this.id,
  meta = mapOf(
    CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionId,
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
    CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceKey,
    CommonRestrictions.TENANT_ID to this.tenantId,
    "processName" to this.processName,
    "taskName" to this.name,
    "assignee" to this.assignee,
    "candidateUsers" to (this.candidateUsers?.joinToString(",") ?: ""),
    "candidateGroups" to (this.candidateGroups?.joinToString(",") ?: ""),
    "formId" to this.formId,
    "formKey" to this.formKey,
    "formVersion" to "${this.formVersion}",
    "creationDate" to this.creationDate.toString(),
    "followUpDate" to (this.followUpDate?.toString() ?: ""),
    "dueDate" to (this.dueDate?.toString() ?: ""),
    "taskState" to this.taskState.name,
  )
)
