package dev.bpmcrafters.example.common.application.usecase;

import dev.bpmcrafters.example.common.application.port.in.DeployInPort;
import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand;
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi;
import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;
import dev.bpmcrafters.processengineapi.deploy.NamedResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class DeployUseCase implements DeployInPort {

  private final DeploymentApi deploymentApi;

  @Override
  public Future<DeploymentInformation> deploy() {
    return deploymentApi.deploy(
      new DeployBundleCommand(
        List.of(
          NamedResource.fromClasspath("simple-process.bpmn")
        ),
        null
      )
    );
  }
}
