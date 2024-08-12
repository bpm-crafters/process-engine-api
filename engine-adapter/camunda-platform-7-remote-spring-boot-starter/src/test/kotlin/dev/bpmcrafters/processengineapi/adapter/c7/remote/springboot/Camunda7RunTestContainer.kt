package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class Camunda7RunTestContainer(tag: String) : GenericContainer<Camunda7RunTestContainer>("camunda/camunda-bpm-platform:$tag") {

  init {
    withCommand("./camunda.sh", "--rest")
    withEnv("CAMUNDA_BPM_DEFAULT-SERIALIZATION-FORMAT", "application/json")
    withExposedPorts(8080)
    addFixedExposedPort(38080, 8080 )
    waitingFor(Wait
      .forHttp("/engine-rest/engine/")
      .forPort(8080)
    )
  }

}
