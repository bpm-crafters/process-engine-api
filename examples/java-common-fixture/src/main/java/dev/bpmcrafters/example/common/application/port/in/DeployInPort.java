package dev.bpmcrafters.example.common.application.port.in;

import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;

import java.util.concurrent.Future;

public interface DeployInPort {
  Future<DeploymentInformation> deploy();
}
