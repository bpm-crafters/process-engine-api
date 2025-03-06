package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity
import org.camunda.bpm.engine.task.IdentityLink
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.camunda.community.mockito.task.TaskFake
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class TaskInformationExtensionsKtTest {

    @Test
    fun `should map Task`() {
        val now = Date.from(Instant.now())
        val task = TaskFake.builder()
            .id("taskId")
            .processDefinitionId("processDefinitionId")
            .processInstanceId("processInstanceId")
            .tenantId("tenantId")
            .taskDefinitionKey("taskDefinitionKey")
            .name("name")
            .description("description")
            .assignee("assignee")
            .createTime(now)
            .followUpDate(now)
            .dueDate(now)
            .formKey("formKey")
            .build()

        val identityLinks =
            listOf(identityLink(groupId = "group"), identityLink(userId = "user-1"), identityLink(userId = "user-2"))

        val taskInformation = task.toTaskInformation(identityLinks, "processDefinitionKey")

        assertThat(taskInformation.taskId).isEqualTo("taskId")
        assertThat(taskInformation.meta[CommonRestrictions.PROCESS_DEFINITION_ID]).isEqualTo("processDefinitionId")
        assertThat(taskInformation.meta[CommonRestrictions.PROCESS_DEFINITION_KEY]).isEqualTo("processDefinitionKey")
        assertThat(taskInformation.meta[CommonRestrictions.TENANT_ID]).isEqualTo("tenantId")
        assertThat(taskInformation.meta["taskName"]).isEqualTo("name")
        assertThat(taskInformation.meta["taskDescription"]).isEqualTo("description")
        assertThat(taskInformation.meta["assignee"]).isEqualTo("assignee")
        assertThat(taskInformation.meta["creationDate"]).isEqualTo(now.toString())
        assertThat(taskInformation.meta["followUpDate"]).isEqualTo(now.toString())
        assertThat(taskInformation.meta["dueDate"]).isEqualTo(now.toString())
        assertThat(taskInformation.meta["formKey"]).isEqualTo("formKey")
        assertThat(taskInformation.meta["candidateUsers"]).isEqualTo("user-1,user-2")
        assertThat(taskInformation.meta["candidateGroups"]).isEqualTo("group")
    }

    @Test
    fun `should map DelegateTask`() {
        val now = Date.from(Instant.now())

        var delegateTask = DelegateTaskFake("taskId")
        delegateTask = delegateTask.withProcessDefinitionId("processDefinitionId")
        delegateTask = delegateTask.withProcessInstanceId("processInstanceId")
        delegateTask = delegateTask.withTenantId("tenantId")
        delegateTask = delegateTask.withTaskDefinitionKey("taskDefinitionKey")
        delegateTask = delegateTask.withName("name")
        delegateTask = delegateTask.withDescription("description")
        delegateTask = delegateTask.withAssignee("assignee")
        delegateTask = delegateTask.withCreateTime(now)
        delegateTask = delegateTask.withFollowUpDate(now)
        delegateTask.dueDate = now
        delegateTask.addGroupIdentityLink("group-1", "CANDIDATE")
        delegateTask.addGroupIdentityLink("group-2", "CANDIDATE")
        delegateTask.addUserIdentityLink("user-1", "CANDIDATE")
        delegateTask.addUserIdentityLink("user-2", "CANDIDATE")

        val taskInformation = delegateTask.toTaskInformation()

        assertThat(taskInformation.taskId).isEqualTo("taskId")
        assertThat(taskInformation.meta[CommonRestrictions.PROCESS_DEFINITION_ID]).isEqualTo("processDefinitionId")
        assertThat(taskInformation.meta[CommonRestrictions.ACTIVITY_ID]).isEqualTo("taskDefinitionKey")
        assertThat(taskInformation.meta[CommonRestrictions.TENANT_ID]).isEqualTo("tenantId")
        assertThat(taskInformation.meta["taskName"]).isEqualTo("name")
        assertThat(taskInformation.meta["taskDescription"]).isEqualTo("description")
        assertThat(taskInformation.meta["assignee"]).isEqualTo("assignee")
        assertThat(taskInformation.meta["creationDate"]).isEqualTo(now.toString())
        assertThat(taskInformation.meta["followUpDate"]).isEqualTo(now.toString())
        assertThat(taskInformation.meta["dueDate"]).isEqualTo(now.toString())
        assertThat(taskInformation.meta["formKey"]).isNull()
        assertThat(taskInformation.meta["candidateUsers"]).isEqualTo("user-1,user-2")
        assertThat(taskInformation.meta["candidateGroups"]).isEqualTo("group-1,group-2")

    }


    private fun identityLink(userId: String? = null, groupId: String? = null): IdentityLink {
        val identityLink = IdentityLinkEntity.newIdentityLink()
        identityLink.userId = userId
        identityLink.groupId = groupId
        return identityLink
    }


}
