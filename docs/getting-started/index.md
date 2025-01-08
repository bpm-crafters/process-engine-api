If you want to try out `process-engine-api` library in your project, there are two steps you need to perform.

First, add the library to your project classpath. In Apache Maven it is just adding the following dependency into your 
project's `pom.xml`:

```xml 
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api</groupId>
    <artifactId>process-engine-api</artifactId>
    <version>${process-engine-api.version}</version>
  </dependency>
```

This dependency provides you with the most important classes required for implementation of your system using
a process engine. Here is an example how a user task can be completed:

```java

import dev.bpmcrafters.processengineapi.task.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompleteUserTaskUseCase implements CompleteUserTaskInPort {

  private final UserTaskCompletionApi taskCompletionApi;

  @Override
  public void completeUserTaskWithSomeValue(String taskId, String value) {
    taskCompletionApi.completeTask(
      new CompleteTaskCmd(
        taskId,
        () -> Map.of("some-user-value", value)
      )
    ).get();
  }

}

```

As you can see, the code above doesn't contain any engine-specific code, but rather uses only code from `process-engine-api`.
This means, that the resulting code is portable and the decision about the used engine doesn't influence the implementation
of your application logic.

The second step depends on your target architecture and used process engine. Please refer to one of the following
configurations:

## Camunda Platform 7 running as embedded engine

If you start with a Camunda Platform 7, operated in an embedded engine mode, by for example using the Camunda Spring Boot Starter,
the following configuration is applicable for you. 

First of all, add the corresponding adapter to your project's classpath:

```xml 
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api</groupId>
    <artifactId>process-engine-api-adapter-camunda-platform-c7-embedded-spring-boot-starter</artifactId>
    <version>${process-engine-api.version}</version>
  </dependency>
```

and finally, add the following configuration to your configuration properties. Here is a version for `application.yaml`:

```yaml 
dev:
  bpm-crafters:
    process-api:
      adapter:
        c7embedded:
          enabled: true
          service-tasks:
            delivery-strategy: embedded_scheduled
            worker-id: embedded-worker
            lock-time-in-seconds: 10
            execute-initial-pull-on-startup: true
            schedule-delivery-fixed-rate-in-seconds: 5
          user-tasks:
            delivery-strategy: embedded_scheduled
            execute-initial-pull-on-startup: true
            schedule-delivery-fixed-rate-in-seconds: 5

```

## Camunda Platform 7 running as remote engine

If you start with a Camunda Platform 7, operated remotely, the following configuration is applicable for you.

First of all add the corresponding adapter to your project's classpath. In order to connect to remote engine,
you will need to use some client. Assuming you are using Camunda Hub extension [camunda-platform-7-rest-client-spring-boot](https://github.com/camunda-community-hub/camunda-platform-7-rest-client-spring-boot),
you will also need to add some additional libraries. Here is the result:

```xml

<dependendcies>
  <!-- the correct adapter -->
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api</groupId>
    <artifactId>process-engine-api-adapter-camunda-platform-c7-remote-spring-boot-starter</artifactId>
    <version>${process-engine-api.version}</version>
  </dependency>
  <!-- rest client library --> 
  <dependency>
    <groupId>org.camunda.community.rest</groupId>
    <artifactId>camunda-platform-7-rest-client-spring-boot-starter</artifactId>
    <version>7.22.0</version>
  </dependency>
  <!-- open feign client -->
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
  </dependency>
</dependendcies>

```

And finally, add the following configuration to your configuration properties. Here is a version for `application.yaml`:

```yaml 
dev:
  bpm-crafters:
    process-api:
      adapter:
        c7remote:
          enabled: true
          service-tasks:
            delivery-strategy: remote_scheduled
            schedule-delivery-fixed-rate-in-seconds: 10
            worker-id: embedded-worker
            lock-time-in-seconds: 10
          user-tasks:
            delivery-strategy: remote_scheduled
            schedule-delivery-fixed-rate-in-seconds: 10

# to tell the client library where the engine is located provide the correct details below:
feign:
  client:
    config:
      default:
        url: "http://localhost:9090/engine-rest/"

```

## Camunda Platform 8 as SaaS

If you start with a Camunda Platform 8, operated as SaaS, the following configuration is applicable for you.

First add the corresponding adapter to your project's classpath:

```xml 
<dependencies>
    <dependency>
      <groupId>dev.bpm-crafters.process-engine-api</groupId>
      <artifactId>process-engine-api-adapter-camunda-platform-c8-spring-boot-starter</artifactId>
    </dependency>

    <!-- We need the camunda client too -->
    <dependency>
      <groupId>io.camunda</groupId>
      <artifactId>spring-boot-starter-camunda-sdk</artifactId>
      <version>8.6.3</version>
    </dependency>
    <dependency>
      <groupId>io.camunda</groupId>
      <artifactId>camunda-tasklist-client-java</artifactId>
      <version>8.6.0</version>
      <exclusions>
        <exclusion>
          <groupId>io.camunda</groupId>
          <artifactId>zeebe-client-java</artifactId>
        </exclusion>
      </exclusions>
    </dependency>

</dependencies>
```

and configure the adapter in your application properties:

```yaml

dev:
  bpm-crafters:
    process-api:
      adapter:
        c8:
          enabled: true
          service-tasks:
            delivery-strategy: subscription
            worker-id: worker
          user-tasks:
            delivery-strategy: subscription_refreshing
            schedule-delivery-fixed-rate-in-seconds: 5000 # every 5 seconds
            tasklist-url: https://${zeebe.client.cloud.region}.tasklist.camunda.io/${zeebe.client.cloud.clusterId}
            fixed-rate-refresh-rate: 5000 # every 5 seconds

camunda:
  client:
    mode: saas
    region: ${ZEEBE_REGION}
    cluster-id: ${ZEEBE_CLUSTER_ID}
    auth:
      clientId: ${ZEEBE_CLIENT_ID}
      clientSecret: ${ZEEBE_CLIENT_SECRET}

```
