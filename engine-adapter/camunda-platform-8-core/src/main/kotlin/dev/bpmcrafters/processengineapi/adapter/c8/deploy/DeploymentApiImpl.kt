package dev.bpmcrafters.processengineapi.adapter.c8.deploy

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.DeploymentEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class DeploymentApiImpl(
  private val zeebeClient: ZeebeClient
) : DeploymentApi {

  override fun deploy(cmd: DeployBundleCommand): Future<DeploymentInformation> {
    require(cmd.resources.isNotEmpty()) { "Resources must not be empty, at least one resource must be provided." }
    val first = cmd.resources.first()
    return CompletableFuture.supplyAsync {
      zeebeClient
        .newDeployResourceCommand()
        .addResourceStream(first.resourceStream, first.name)
        .apply {
          if (cmd.resources.size > 1) {
            val remaining = cmd.resources.subList(1, cmd.resources.size)
            remaining.forEach { resource -> this.addResourceStream(resource.resourceStream, resource.name) }
          }
          if (cmd.tenantId != null) {
            this.tenantId(cmd.tenantId)
          }
        }
        .send()
        .get()
        .toDeploymentInformation()
    }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  private fun DeploymentEvent.toDeploymentInformation() = DeploymentInformation(
    deploymentKey = "${this.key}"
    // FIXME -> tenant, process definitions, decision definitions
  )
}
