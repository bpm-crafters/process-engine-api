# Process Engine API


[![stable](https://img.shields.io/badge/lifecycle-STABLE-green.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Development branches](https://github.com/bpm-crafters/process-engine-api/actions/workflows/development.yml/badge.svg)](https://github.com/bpm-crafters/process-engine-api/actions/workflows/development.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.bpm-crafters.process-engine-api/process-engine-api)](https://central.sonatype.com/namespace/dev.bpm-crafters.process-engine-api)

## Purpose of the library

This library provides a modern engine-agnostic API which can be used to implement process applications. By providing a set
of adapters to relevant process engines (Camunda Platform 7, Camunda Platform 8, etc...) the library enforces separation of 
the integration of process engine from the selection of the used engine. This approach fosters an easy migration between engines 
and tries to achieve to support migrations with minimal (or even no) code modifications. 

## API

The API consists of different parts independent of each other.

- Deployment API
- Process API
- Correlation API
- Signal API
- Task Subscription API
- User Task Completion API
- User Task Modification API
- Service Task Completion API

## Helpful Components

- User Task Support

## Available Engine Adapters
 
If you want to try the API, please refer to one of the adapter implementations matching your infrastructure. For example:

- [Camunda Platform 7 Adapter](https://github.com/bpm-crafters/process-engine-adapters-camunda-7) [![Maven Central Version](https://img.shields.io/maven-central/v/dev.bpm-crafters.process-engine-adapters/process-engine-adapter-camunda-platform-c7-bom?color=#2cc657)](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-adapters/process-engine-adapter-camunda-platform-c7-bom)
- [Camunda Platform 8 Adapter](https://github.com/bpm-crafters/process-engine-adapters-camunda-8) [![Maven Central Version](https://img.shields.io/maven-central/v/dev.bpm-crafters.process-engine-adapters/process-engine-adapter-camunda-platform-c8-bom?color=#2cc657)](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-adapters/process-engine-adapter-camunda-platform-c8-bom)

## Process Engine Worker

If you are using the Process Engine API to provide workers using Spring Boot, there is a library with improved support for it:

- [Process Engine Worker](https://github.com/bpm-crafters/process-engine-worker) [![Maven Central Version](https://img.shields.io/maven-central/v/dev.bpm-crafters.process-engine-worker/process-engine-worker-spring-boot-starter?color=#2cc657)](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-worker/process-engine-worker-spring-boot-starter)


## Anatomy

The library contains of the following Maven modules:

- `process-engine-api`: pure API written in Kotlin (100% Java-compatible)
- `process-engine-api-impl`: commons implementation, which is independent of the selected engine and can be used for adapter implementations.

## Contribution and Development

If you are missing a feature or found a bug, please [open an issue](https://github.com/bpm-crafters/process-engine-api/issues) 
on this project and provide a pull request. If you have general questions, make sure to stop by our [discussions](https://github.com/orgs/bpm-crafters/discussions).

