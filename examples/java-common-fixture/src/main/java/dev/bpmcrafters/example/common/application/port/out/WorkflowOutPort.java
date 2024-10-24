package dev.bpmcrafters.example.common.application.port.out;

import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;

/**
 * Workflow outbound port to support all activities around process instance.
 */
public interface WorkflowOutPort {

  /**
   * Starts new process instance.
   * @param value example string value.
   * @param intValue example intValue.
   * @return instance id.
   */
  String startSimpleProcess(String value, Integer intValue);

  void deliverSignal(String variableValue);

  void correlateMessage(String correlationValue, String variableValue);

  DeploymentInformation deploySimpleProcess();

}
