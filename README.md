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
- `process-engine-api-impl`: commons implementation, which are independent of the selected engine

## API

The API consists of different parts independent of each other.

- Deployment API
- Process API
- Correlation API
- Signal API
- Task Subscription API
- User Task Completion API
- Service Task Completion API

The Task API provides functionality to deal with service tasks. The task handlers can be registered and get invoked when tasks 
appear in the process engine. Since the Task API allows asynchronous processing, we provide a special API to complete tasks.
 
If you want to try the API, please refer to one of the adapter implementations matching your infrastructure. For example:

- https://github.com/bpm-crafters/process-engine-adapters-camunda-7
- https://github.com/bpm-crafters/process-engine-adapters-camunda-8


