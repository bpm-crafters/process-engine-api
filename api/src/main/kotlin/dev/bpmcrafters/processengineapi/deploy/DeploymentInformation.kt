package dev.bpmcrafters.processengineapi.deploy

import java.time.Instant

data class DeploymentInformation(
  val deploymentKey: String,
  val deploymentTime: Instant?,
  val tenantId: String?
)
