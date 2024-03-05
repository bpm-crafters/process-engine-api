# Process Engine API


[![incubating](https://img.shields.io/badge/lifecycle-INCUBATING-orange.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Development branches](https://github.com/bpm-crafters/process-engine-api/actions/workflows/development.yml/badge.svg)](https://github.com/bpm-crafters/process-engine-api/actions/workflows/development.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-api/process-engine-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-api/process-engine-api)

## Purpose of the library

This library provides a modern engine-agnostic API which can be used to implement process applications. By providing a set
of adapters to relevant process engines (Camunda Platform 7, Camunda Platform 8, etc...) the library enforces separation of 
the integration of process engine from the selection of the used engine. This approach fosters an easy migration between engines 
and tries to achieve to support migrations with minimal (or even no) code modifications. 

## Anatomy

The library contains of the following Maven modules:

- `process-engine-api`: pure API written in Kotlin (100% Java-compatible)
- `process-engine-api-adapter-commons`: commons for adapter implementation, which are independent of the selected engine
- `process-engine-api-adapter-camunda-platform-7-embedded-core`: core implementation classes for Camunda 7 Platform without additional dependencies
- `process-engine-api-adapter-camunda-platform-7-embedded-spring-boot-starter`: SpringBoot starter for usage of Camunda 7 Platform adapter
- `process-engine-api-adapter-camunda-platform-8-core`: core implementation classes for Camunda 8 Platform without additional dependencies
- `process-engine-api-adapter-camunda-platform-8-spring-boot-starter`: SpringBoot starter for usage of Camunda 8 Platform adapter
- `examples/java-common-fixture`: A project that is used independent of selected process engine adapter 
- `examples/java-c7`: Project with embedded C7 engine and a simple process scenario 
- `examples/java-c8`: Project with configuration of SaaS C8 engine and a simple process scenario 

## API

The API consists of different parts independent of each other.

### Process API

The Process API provides functionality, required to control the lifecycle of the processes. It allows to start a new process instance.
It is intended to be used in outbound adapters of the port/adapter architecture in order to control the process engine from your application.

### Correlation API

The Correlation API provides functionality to correlate messages and signals with running process instances.
It is intended to be used in outbound adapters of the port/adapter architecture in order to control the process engine from your application.

### Signal API

The Correlation API provides functionality to correlate messages and signals with running process instances.
It is intended to be used in outbound adapters of the port/adapter architecture in order to control the process engine from your application.


### Task API

The Task API provides functionality to deal with tasks. The task handlers can be registered and get invoked when tasks 
appear in the process engine. Since the Task API allows asynchronous processing, we provide a special API to complete tasks.
 
## Usage

If you want to try the library with Camunda 7, please add the following dependency to your Maven `pom.xml`.

```xml
<dependency>
  <groupId>dev.bpm-crafters.process-engine-api</groupId>
  <artifactId>process-engine-api-adapter-camunda-platform-c7-embedded-spring-boot-starter</artifactId>
</dependency>
```

If you want to try the library with Camunda 8, please add the following dependency to your Maven `pom.xml`.

```xml
<dependency>
  <groupId>dev.bpm-crafters.process-engine-api</groupId>
  <artifactId>process-engine-api-adapter-camunda-platform-c8-spring-boot-starter</artifactId>
</dependency>
```
