package dev.bpmcrafters.example.common.application.usecase;

import dev.bpmcrafters.example.common.application.port.in.DeployInPort;
import dev.bpmcrafters.example.common.application.port.out.WorkflowOutPort;
import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class DeployUseCase implements DeployInPort {

  private final WorkflowOutPort workflowOutPort;

  @Override
  public Future<DeploymentInformation> deploy() {
    return CompletableFuture.completedFuture(workflowOutPort.deploySimpleProcess());
  }
}
