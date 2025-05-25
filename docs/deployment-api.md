---
title: Deployment API
---

The Deployment API allows to deploy process-related resources in a bundle into the engine.

Here is an example of usage:

```java

@Component
@RequiredArgsConstructor
public class Deployer {

  private final DeploymentApi deploymentApi;

  @SneakyThrows
  public DeploymentInformation deploy(List<String> resourceNames) {
    var info = deploymentApi.deploy(
      new DeployBundleCommand(
        resources.stream.map(resourceName -> NamedResource.fromClasspath(resourceName)).toList(),
        null // tenant id
      )
    ).get();
    return info;
  }
}


```
