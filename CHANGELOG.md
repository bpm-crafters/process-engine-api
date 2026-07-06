# Changelog

## [1.7] - 2026-06-24

### :rocket: Enhancements & Features

- docs(#296): clarify task subscription delivery semantics [#297](https://github.com/bpm-crafters/process-engine-api/pull/297)
- Add StartProcessByMessageAtElementCmd to Process API [#292](https://github.com/bpm-crafters/process-engine-api/issues/292)

### :heart: Contributors

Thank you to all the contributors who worked on this release:

@emaarco and @lmoesle

## [1.6] - 2026-06-03

### :rocket: Enhancements & Features

- Implement ChangeDatesModifyTaskCmd [#285](https://github.com/bpm-crafters/process-engine-api/pull/285)
- Transaction-aware command execution via ExecutionMode [#281](https://github.com/bpm-crafters/process-engine-api/issues/281)

### :bug: Bug Fixes

- Broken docs link "here" [#276](https://github.com/bpm-crafters/process-engine-api/issues/276)

### :hammer_and_wrench: Chore

- Bump dev.bpm-crafters.maven.parent:maven-parent from 2026.04.1 to 2026.06.1 [#290](https://github.com/bpm-crafters/process-engine-api/pull/290)
- Bump codecov/codecov-action from 6.0.0 to 6.0.1 [#289](https://github.com/bpm-crafters/process-engine-api/pull/289)
- Bump slf4j.version from 2.0.17 to 2.0.18 [#288](https://github.com/bpm-crafters/process-engine-api/pull/288)
- Bump dev.bpm-crafters.maven.parent:maven-parent from 2026.02.1 to 2026.04.1 [#286](https://github.com/bpm-crafters/process-engine-api/pull/286)
- Bump codecov/codecov-action from 5.5.3 to 6.0.0 [#284](https://github.com/bpm-crafters/process-engine-api/pull/284)
- Bump org.mockito.kotlin:mockito-kotlin from 6.2.3 to 6.3.0 [#283](https://github.com/bpm-crafters/process-engine-api/pull/283)
- Bump codecov/codecov-action from 5.5.2 to 5.5.3 [#282](https://github.com/bpm-crafters/process-engine-api/pull/282)
- Bump dev.bpm-crafters.maven.parent:maven-parent from 2025.08.1 to 2026.02.1 [#279](https://github.com/bpm-crafters/process-engine-api/pull/279)

### :heart: Contributors

Thank you to all the contributors who worked on this release:

@julianwr and @zambrovski

## [1.5] - 2026-02-05

### :zap: Breaking Changes

- As a user I want to pass payload to commands that contain null values [#268](https://github.com/bpm-crafters/process-engine-api/issues/268), [#273](https://github.com/bpm-crafters/process-engine-api/pull/273)
- Redesign decision evaluation result [#262](https://github.com/bpm-crafters/process-engine-api/pull/262)

### :rocket: Enhancements & Features
- Add support for starting process instances at specific elements [#265](https://github.com/bpm-crafters/process-engine-api/issues/265), [#266](https://github.com/bpm-crafters/process-engine-api/pull/266)


### :hammer_and_wrench: Chore

- Bump org.mockito.kotlin:mockito-kotlin from 6.2.2 to 6.2.3 [#272](https://github.com/bpm-crafters/process-engine-api/pull/272)
- Bump org.mockito.kotlin:mockito-kotlin from 6.2.1 to 6.2.2 [#271](https://github.com/bpm-crafters/process-engine-api/pull/271)
- Bump org.assertj:assertj-core from 3.27.6 to 3.27.7 [#270](https://github.com/bpm-crafters/process-engine-api/pull/270)
- Bump org.mockito.kotlin:mockito-kotlin from 6.1.0 to 6.2.1 [#267](https://github.com/bpm-crafters/process-engine-api/pull/267)
- Bump codecov/codecov-action from 5.5.1 to 5.5.2 [#263](https://github.com/bpm-crafters/process-engine-api/pull/263)
- Bump actions/checkout from 5 to 6 [#261](https://github.com/bpm-crafters/process-engine-api/pull/261)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)
- [@emaarco](https://github.com/emaarco)
- [@dmytro-gundartsev](https://github.com/dmytro-gundartsev)


## [1.4] - 2025-11-10

### :zap: Breaking Changes

- Change Async API from `Future<T>` to `CompletableFuture<T>` [#259](https://github.com/bpm-crafters/process-engine-api/pull/259)

### :rocket: Enhancements & Features

- Implement evaluate decision API [#260](https://github.com/bpm-crafters/process-engine-api/pull/260)
- Correlate message after event-based gateway [#251](https://github.com/bpm-crafters/process-engine-api/issues/251)
- Introduce API to evaluate decisions (DMN) [#243](https://github.com/bpm-crafters/process-engine-api/issues/243)

### :hammer_and_wrench: Chore

- Bump org.mockito.kotlin:mockito-kotlin from 6.0.0 to 6.1.0 [#255](https://github.com/bpm-crafters/process-engine-api/pull/255)
- Bump org.assertj:assertj-core from 3.27.5 to 3.27.6 [#254](https://github.com/bpm-crafters/process-engine-api/pull/254)
- Bump org.assertj:assertj-core from 3.27.4 to 3.27.5 [#253](https://github.com/bpm-crafters/process-engine-api/pull/253)
- Bump codecov/codecov-action from 5.5.0 to 5.5.1 [#252](https://github.com/bpm-crafters/process-engine-api/pull/252)
- Bump actions/setup-java from 4 to 5 [#250](https://github.com/bpm-crafters/process-engine-api/pull/250)
- Bump dev.bpm-crafters.maven.parent:maven-parent from 2025.07.3 to 2025.08.1 [#248](https://github.com/bpm-crafters/process-engine-api/pull/248)
- Bump actions/checkout from 4 to 5 [#247](https://github.com/bpm-crafters/process-engine-api/pull/247)
- Bump org.assertj:assertj-core from 3.27.3 to 3.27.4 [#246](https://github.com/bpm-crafters/process-engine-api/pull/246)
- Bump org.mockito.kotlin:mockito-kotlin from 5.4.0 to 6.0.0 [#245](https://github.com/bpm-crafters/process-engine-api/pull/245)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)
- [@rohwerj](https://github.com/rohwerj)


## [1.3] - 2025-07-11

### :rocket: Enhancements & Features

- Add task modification API [#240](https://github.com/bpm-crafters/process-engine-api/pull/240)
- Provide new operation "save user task" [#238](https://github.com/bpm-crafters/process-engine-api/issues/238)

### :hammer_and_wrench: Chore

- Switch to new parent and publishing system [#239](https://github.com/bpm-crafters/process-engine-api/issues/239)
- Bump dev.bpm-crafters.maven.parent:maven-parent from 2025.04.2 to 2025.05.1 [#237](https://github.com/bpm-crafters/process-engine-api/pull/237)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [1.2] - 2025-05-25

### :zap: Breaking Changes

* Feature/full tenant support by @zambrovski in https://github.com/bpm-crafters/process-engine-api/pull/236

### :hammer_and_wrench: Chore

* Bump codecov/codecov-action from 5.4.2 to 5.4.3 by @dependabot in https://github.com/bpm-crafters/process-engine-api/pull/234

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [1.1] - 2025-04-15

### :zap: Breaking Changes

- Task handlers with reasons and value based commands [#227](https://github.com/bpm-crafters/process-engine-api/pull/227)
- Extend the TaskTerminationHandler to allow to pass TaskInformation [#223](https://github.com/bpm-crafters/process-engine-api/issues/223)

### :rocket: Enhancements & Features

- Support detection of assignment in user task support [#226](https://github.com/bpm-crafters/process-engine-api/issues/226)
- Extends task completion commands to be used with plain objects instead of suppliers [#224](https://github.com/bpm-crafters/process-engine-api/issues/224)

### :hammer_and_wrench: Chore

- Bump dev.bpm-crafters.maven.parent:maven-parent from 2025.04.1 to 2025.04.2 [#229](https://github.com/bpm-crafters/process-engine-api/pull/229)
- Bump codecov/codecov-action from 5.4.0 to 5.4.2 [#228](https://github.com/bpm-crafters/process-engine-api/pull/228)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [1.0] - 2025-04-01

### :zap: Breaking release, separating artefacts

- Stable public release 1.0 including latest version of the API. Separates API from adapter implementations. The coordinates for the API are `dev.bpm-crafters.process-engine-api:process-engine-api`. Latest released version is `1.0`
- Camunda Platform 7 Adapters are now served as a separate artefact. Coordinates are: `dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c7-remote-spring-boot-starter` and `dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c7-embedded-spring-boot-starter`. Latest released version is `2025.04.1` . See https://github.com/bpm-crafters/process-engine-adapters-camunda-7
- Camunda Platform 8 Adapters are now served as a separate artefact. Coordinates are: `dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c8-spring-boot-starter`. Latest released version is `2025.04.1`. See https://github.com/bpm-crafters/process-engine-adapters-camunda-8

## [0.9.0] - 2025-03-31

### :zap: Breaking Changes

- Perform repository separation [#222](https://github.com/bpm-crafters/process-engine-api/pull/222)
- Separate API from adapters and Docs [#221](https://github.com/bpm-crafters/process-engine-api/issues/221)

For now, please make sure you reference the adapters directly from their corresponding repositories. Currently, the new
group id for Camunda adapters is `dev.bpm-crafters.process-engine-adapters` with artifact ids: `process-engine-adapters-camunda-platform-c7-remote-spring-boot-starter`, `process-engine-adapters-camunda-platform-c7-embedded-spring-boot-starter`
and `process-engine-adapters-camunda-platform-c8-spring-boot-starter`

### :hammer_and_wrench: Chore

- Bump org.springframework.boot:spring-boot-dependencies from 3.4.3 to 3.4.4 [#220](https://github.com/bpm-crafters/process-engine-api/pull/220)
- Bump org.springdoc:springdoc-openapi-starter-webmvc-ui from 2.8.5 to 2.8.6 [#219](https://github.com/bpm-crafters/process-engine-api/pull/219)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.5.0] - 2025-03-06

### :zap: Breaking Changes

- Remove restriction `taskDefinitionKey` [#209](https://github.com/bpm-crafters/process-engine-api/pull/209)
- Refactor, separate and rename modules [#200](https://github.com/bpm-crafters/process-engine-api/pull/200)

### How to migrate

 - `process-engine-api-adapter-commons`doesn't exist anymore, but the implementation code is moved to the new `process-engine-api-impl`module. If you are the user of the process engine api, you should not notice this change and should not have the implementation dependency. 
 - The `UserTaskSupport` has moved to `process-engine-api`and is now located in `dev.bpmcrafters.processengineapi.task.support.UserTaskSupport` package.
 - If you used `CommonRestrictions.TASK_DEFINITION_KEY` restriction, you should either skip it, or switch to a more generic `ACTIVITY_ID`. In addition, check if provisioning of the `taskDefinitionKey` during the subscription solves your problem. Inside the `TaskInformation` the name of the XML element (id of the activity defining the task) is mapped into the `activityId` value. 


### :rocket: Enhancements & Features

- Get rid of `process-engine-api-adapter-commons-spring-boot-starter` module [#196](https://github.com/bpm-crafters/process-engine-api/pull/196)
- Get rid of `process-engine-api-adapter-commons-spring-boot-starter` module [#195](https://github.com/bpm-crafters/process-engine-api/issues/195)
- update process coverage - fix klogging dependency [#184](https://github.com/bpm-crafters/process-engine-api/issues/184)

### :hammer_and_wrench: Chore

- Bump org.slf4j:slf4j-api from 2.0.16 to 2.0.17 [#207](https://github.com/bpm-crafters/process-engine-api/pull/207)
- Bump io.camunda:zeebe-process-test-extension-testcontainer from 8.6.9 to 8.6.10 [#206](https://github.com/bpm-crafters/process-engine-api/pull/206)
- Bump io.camunda:zeebe-process-test-assertions from 8.6.9 to 8.6.10 [#205](https://github.com/bpm-crafters/process-engine-api/pull/205)
- Bump org.slf4j:slf4j-simple from 2.0.16 to 2.0.17 [#204](https://github.com/bpm-crafters/process-engine-api/pull/204)
- Bump io.camunda:zeebe-client-java from 8.6.9 to 8.6.10 [#203](https://github.com/bpm-crafters/process-engine-api/pull/203)
- Bump io.mockk:mockk-jvm from 1.13.16 to 1.13.17 [#202](https://github.com/bpm-crafters/process-engine-api/pull/202)
- Bump io.camunda:spring-boot-starter-camunda-sdk from 8.6.9 to 8.6.10 [#201](https://github.com/bpm-crafters/process-engine-api/pull/201)
- Bump codecov/codecov-action from 5.3.1 to 5.4.0 [#197](https://github.com/bpm-crafters/process-engine-api/pull/197)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.4.4] - 2025-02-26

### :bug: Bug Fixes

- Correct task matching for process definition key restriction [#194](https://github.com/bpm-crafters/process-engine-api/pull/194)
- Adapter C7CamundaEmbedded ignores PROCESS_DEFINITION_KEY restriction in EmbeddedUserTaskPullDelivery [#193](https://github.com/bpm-crafters/process-engine-api/issues/193)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.4.3] - 2025-02-24

### :rocket: Enhancements & Features

- C7remote adapter should close opened subscription on termination if `remote_subscribed` strategy is used [#186](https://github.com/bpm-crafters/process-engine-api/issues/186)

### :bug: Bug Fixes

- Fix strategy remote_subscribed [#187](https://github.com/bpm-crafters/process-engine-api/pull/187)
- Make c7remote work with original camunda-external-task-client [#185](https://github.com/bpm-crafters/process-engine-api/issues/185)

### :hammer_and_wrench: Chore

- Bump org.awaitility:awaitility-kotlin from 4.2.2 to 4.3.0 [#192](https://github.com/bpm-crafters/process-engine-api/pull/192)
- Bump org.springframework.boot:spring-boot-dependencies from 3.4.2 to 3.4.3 [#191](https://github.com/bpm-crafters/process-engine-api/pull/191)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@jangalinski](https://github.com/jangalinski)


## [0.4.2] - 2025-02-24

### :bug: Bug Fixes

- fix: update klogging [#183](https://github.com/bpm-crafters/process-engine-api/pull/183)
- Fix klogging exclusion [#182](https://github.com/bpm-crafters/process-engine-api/issues/182)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@jangalinski](https://github.com/jangalinski)


## [0.4.1] - 2025-02-20

### :rocket: Enhancements & Features

- Improve tenant support [#159](https://github.com/bpm-crafters/process-engine-api/pull/159)
- Improve support for tenant-aware task subscriptions [#156](https://github.com/bpm-crafters/process-engine-api/issues/156)
- Error message should be passed to Zeebe for failure oder error [#155](https://github.com/bpm-crafters/process-engine-api/issues/155)

### :bug: Bug Fixes

- Exclude dependency io.github.microutils:kotlin-logging-jvm [#171](https://github.com/bpm-crafters/process-engine-api/issues/171)
- feat: Send error details to Zeebe, closes #155 [#158](https://github.com/bpm-crafters/process-engine-api/pull/158)

### :hammer_and_wrench: Chore

- Bump io.camunda:zeebe-process-test-extension-testcontainer from 8.6.7 to 8.6.9 [#181](https://github.com/bpm-crafters/process-engine-api/pull/181)
- Bump io.camunda:zeebe-client-java from 8.6.6 to 8.6.9 [#180](https://github.com/bpm-crafters/process-engine-api/pull/180)
- Bump io.camunda:spring-boot-starter-camunda-sdk from 8.6.6 to 8.6.9 [#179](https://github.com/bpm-crafters/process-engine-api/pull/179)
- Bump org.springdoc:springdoc-openapi-starter-webmvc-ui from 2.8.4 to 2.8.5 [#178](https://github.com/bpm-crafters/process-engine-api/pull/178)
- Bump io.camunda:zeebe-process-test-assertions from 8.6.7 to 8.6.9 [#177](https://github.com/bpm-crafters/process-engine-api/pull/177)
- Bump org.springdoc:springdoc-openapi-starter-webmvc-ui from 2.8.0 to 2.8.4 [#170](https://github.com/bpm-crafters/process-engine-api/pull/170)
- Bump org.springframework.boot:spring-boot-dependencies from 3.4.0 to 3.4.2 [#169](https://github.com/bpm-crafters/process-engine-api/pull/169)
- Bump codecov/codecov-action from 5.1.2 to 5.3.1 [#168](https://github.com/bpm-crafters/process-engine-api/pull/168)
- Bump org.assertj:assertj-core from 3.27.2 to 3.27.3 [#165](https://github.com/bpm-crafters/process-engine-api/pull/165)
- Bump io.mockk:mockk-jvm from 1.13.14 to 1.13.16 [#160](https://github.com/bpm-crafters/process-engine-api/pull/160)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)
- [@janvonneree](https://github.com/janvonneree)
- [@stefanzilske](https://github.com/stefanzilske)


## [0.4.0] - 2025-01-08

### :rocket: Enhancements & Features

- Set business key when starting a process [#116](https://github.com/bpm-crafters/process-engine-api/issues/116)
- Map more meta fields for C7 embedded [#115](https://github.com/bpm-crafters/process-engine-api/issues/115)

### :hammer_and_wrench: Chore

- Bump io.camunda:zeebe-process-test from 8.6.6 to 8.6.7 [#154](https://github.com/bpm-crafters/process-engine-api/issues/154)
- Bump org.springdoc:springdoc-openapi-starter-webmvc-ui from 2.7.0 to 2.8.0 [#153](https://github.com/bpm-crafters/process-engine-api/pull/153)
- Bump org.assertj:assertj-core from 3.26.3 to 3.27.2 [#152](https://github.com/bpm-crafters/process-engine-api/pull/152)
- Bump com.tngtech.jgiven:jgiven-core from 2.0.0 to 2.0.1 [#149](https://github.com/bpm-crafters/process-engine-api/pull/149)
- Bump io.camunda:camunda-tasklist-client-java from 8.6.4 to 8.6.8 [#148](https://github.com/bpm-crafters/process-engine-api/pull/148)
- Bump dev.bpm-crafters.maven.parent:maven-parent from 2024.10.1 to 2024.12.2 [#146](https://github.com/bpm-crafters/process-engine-api/pull/146)
- Bump io.camunda:zeebe-process-test-assertions from 8.6.5 to 8.6.6 [#145](https://github.com/bpm-crafters/process-engine-api/pull/145)
- Bump io.camunda:spring-boot-starter-camunda-sdk from 8.6.5 to 8.6.6 [#144](https://github.com/bpm-crafters/process-engine-api/pull/144)
- Bump org.springframework.cloud:spring-cloud-dependencies from 2023.0.3 to 2024.0.0 [#143](https://github.com/bpm-crafters/process-engine-api/pull/143)
- Bump com.tngtech.jgiven:jgiven-core from 1.3.1 to 2.0.0 [#142](https://github.com/bpm-crafters/process-engine-api/pull/142)
- Bump io.camunda:zeebe-client-java from 8.6.5 to 8.6.6 [#141](https://github.com/bpm-crafters/process-engine-api/pull/141)
- Bump org.camunda.community.rest:camunda-platform-7-rest-client-spring-boot-starter from 7.22.0 to 7.22.1 [#140](https://github.com/bpm-crafters/process-engine-api/pull/140)
- Bump io.camunda:zeebe-process-test-extension-testcontainer from 8.6.5 to 8.6.6 [#139](https://github.com/bpm-crafters/process-engine-api/pull/139)
- Bump org.springframework.boot:spring-boot-dependencies from 3.3.5 to 3.4.0 [#136](https://github.com/bpm-crafters/process-engine-api/pull/136)
- Bump org.springdoc:springdoc-openapi-starter-webmvc-ui from 2.6.0 to 2.7.0 [#135](https://github.com/bpm-crafters/process-engine-api/pull/135)
- Bump codecov/codecov-action from 5.0.3 to 5.0.7 [#132](https://github.com/bpm-crafters/process-engine-api/pull/132)
- Bump codecov/codecov-action from 4.6.0 to 5.0.3 [#131](https://github.com/bpm-crafters/process-engine-api/pull/131)
- Bump io.camunda:spring-boot-starter-camunda-sdk from 8.6.3 to 8.6.5 [#126](https://github.com/bpm-crafters/process-engine-api/pull/126)
- Bump io.camunda:zeebe-process-test-assertions from 8.6.3 to 8.6.5 [#125](https://github.com/bpm-crafters/process-engine-api/pull/125)
- Bump io.camunda:zeebe-process-test-extension-testcontainer from 8.6.3 to 8.6.5 [#124](https://github.com/bpm-crafters/process-engine-api/pull/124)
- Bump io.camunda:zeebe-client-java from 8.6.3 to 8.6.5 [#123](https://github.com/bpm-crafters/process-engine-api/pull/123)
- Bump camunda.version from 7.22.0 to 7.22.0 [#114](https://github.com/bpm-crafters/process-engine-api/pull/114)
- Bump io.camunda:camunda-tasklist-client-java from 8.6.3 to 8.6.4 [#113](https://github.com/bpm-crafters/process-engine-api/pull/113)
- Bump org.springframework.boot:spring-boot-dependencies from 3.3.4 to 3.3.5 [#112](https://github.com/bpm-crafters/process-engine-api/pull/112)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@MichaelVonB](https://github.com/MichaelVonB)

## [0.3.x] - 2024-10-25

### :rocket: Enhancements & Features

- Implementation for C7 event and pull delivery [#110](https://github.com/bpm-crafters/process-engine-api/pull/110)
- Allow to use event + pull for C7 Adapter [#108](https://github.com/bpm-crafters/process-engine-api/issues/108)
- Create example JGiven / BDD tests [#104](https://github.com/bpm-crafters/process-engine-api/issues/104)

### :hammer_and_wrench: Chore

- Bump io.camunda:camunda-tasklist-client-java from 8.6.0 to 8.6.3 [#107](https://github.com/bpm-crafters/process-engine-api/pull/107)
- Bump dev.bpm-crafters.maven.parent:maven-parent from 2024.9.1 to 2024.10.1 [#106](https://github.com/bpm-crafters/process-engine-api/pull/106)
- Bump org.springframework.boot:spring-boot-dependencies from 3.3.3 to 3.3.4 [#95](https://github.com/bpm-crafters/process-engine-api/pull/95)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)
- [@janvonneree](https://github.com/janvonneree)
- [@stefanzilske](https://github.com/stefanzilske)
- [@p-wunderlich](https://github.com/p-wunderlich)


## [0.2.0] - 2024-10-16

### :rocket: Enhancements & Features

- Support Camunda 7.22 [#103](https://github.com/bpm-crafters/process-engine-api/issues/103)
- Add jgiven support [#102](https://github.com/bpm-crafters/process-engine-api/pull/102)
- Provide support for Jgiven BDD-like tests [#101](https://github.com/bpm-crafters/process-engine-api/issues/101)
- Implement UserTaskSupport component [#90](https://github.com/bpm-crafters/process-engine-api/pull/90)
- Provide a UserTaskSupport for local access to delivered tasks / payloads [#89](https://github.com/bpm-crafters/process-engine-api/issues/89)
- Support Camunda 8.6 [#87](https://github.com/bpm-crafters/process-engine-api/issues/87)

### :hammer_and_wrench: Chore

- Bump io.mockk:mockk-jvm from 1.13.12 to 1.13.13 [#100](https://github.com/bpm-crafters/process-engine-api/pull/100)
- Bump io.camunda:camunda-tasklist-client-java from 8.5.3.6 to 8.6.0 [#99](https://github.com/bpm-crafters/process-engine-api/pull/99)
- Bump zeebe-client-java.version from 8.6.0 to 8.6.3 [#98](https://github.com/bpm-crafters/process-engine-api/pull/98)
- Bump zeebe-client-java.version from 8.5.7 to 8.6.0 [#94](https://github.com/bpm-crafters/process-engine-api/pull/94)
- Bump org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-external-task-client from 7.21.0 to 7.22.0 [#93](https://github.com/bpm-crafters/process-engine-api/pull/93)
- Bump org.camunda.community.mockito:camunda-platform-7-mockito from 7.21.0 to 7.22.0 [#92](https://github.com/bpm-crafters/process-engine-api/pull/92)
- Bump camunda.version from 7.21.0 to 7.22.0 [#91](https://github.com/bpm-crafters/process-engine-api/pull/91)
- Bump codecov/codecov-action from 4.5.0 to 4.6.0 [#88](https://github.com/bpm-crafters/process-engine-api/pull/88)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.1.1] - 2024-09-30

### :rocket: Enhancements & Features

- C8: Support information from custom headers using SUBSCRIBING_REFRESHING  user task delivery strategy [#82](https://github.com/bpm-crafters/process-engine-api/issues/82)

### :hammer_and_wrench: Chore

- Bump org.springframework.boot:spring-boot-dependencies from 3.3.3 to 3.3.4 [#83](https://github.com/bpm-crafters/process-engine-api/pull/83)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.1.0] - 2024-09-17

### :rocket: Enhancements & Features

- Improving C8 Adapter [#81](https://github.com/bpm-crafters/process-engine-api/pull/81)
- Disallow embedded_job for service tasks [#76](https://github.com/bpm-crafters/process-engine-api/issues/76)

### :bug: Bug Fixes

- Conditions only work if the value is defined in the properties. [#79](https://github.com/bpm-crafters/process-engine-api/issues/79)

### :hammer_and_wrench: Chore

- Bump dev.bpm-crafters.maven.parent:maven-parent from 2024.8.1 to 2024.9.1 [#77](https://github.com/bpm-crafters/process-engine-api/pull/77)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.0.6] - 2024-09-02

### :rocket: Enhancements & Features

- Provide explicit Spring Conditions for delivery modes in adapters [#74](https://github.com/bpm-crafters/process-engine-api/issues/74)
- camunda 7 remote adapter  improvements [#73](https://github.com/bpm-crafters/process-engine-api/pull/73)
- C7-remote: Use declarative schedules and explicit initial pull config [#72](https://github.com/bpm-crafters/process-engine-api/issues/72)
- Adopt changes from C7embedded to C7remote [#70](https://github.com/bpm-crafters/process-engine-api/issues/70)

### :hammer_and_wrench: Chore

- Support Camunda 8.5 [#57](https://github.com/bpm-crafters/process-engine-api/issues/57)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@p-wunderlich](https://github.com/p-wunderlich)
- [@zambrovski](https://github.com/zambrovski)


## [0.0.5] - 2024-08-27

### :bug: Bug Fixes

- Async in pull-strategy [#67](https://github.com/bpm-crafters/process-engine-api/issues/67)

### :hammer_and_wrench: Chore

- Bump org.springframework.boot:spring-boot-dependencies from 3.3.2 to 3.3.3 [#65](https://github.com/bpm-crafters/process-engine-api/pull/65)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.0.4] - 2024-08-15

### :zap: Breaking

- Rename external tasks to more consistent "service tasks" [#60](https://github.com/bpm-crafters/process-engine-api/issues/60)

### :bug: Bug Fixes

- Make sure (complex) variables are deserialized when using embedded C7 with scheduled external task delivery [#59](https://github.com/bpm-crafters/process-engine-api/issues/59)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.0.3] - 2024-08-14

### :bug: Bug Fixes

- C8 Adapter ignores enabled flag for some bean creation [#58](https://github.com/bpm-crafters/process-engine-api/issues/58)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.0.2] - 2024-08-14

### :rocket: Enhancements & Features

- Create local setup with docker-compose [#42](https://github.com/bpm-crafters/process-engine-api/issues/42)
- Allow multiple adapter implementations in runtime at the same time  [#39](https://github.com/bpm-crafters/process-engine-api/issues/39)

### :hammer_and_wrench: Chore

- Bump org.springdoc:springdoc-openapi-starter-webmvc-ui from 2.3.0 to 2.6.0 [#56](https://github.com/bpm-crafters/process-engine-api/pull/56)
- Bump com.h2database:h2 from 2.2.224 to 2.3.232 [#55](https://github.com/bpm-crafters/process-engine-api/pull/55)
- Bump org.springframework.boot:spring-boot-dependencies from 3.2.5 to 3.3.2 [#54](https://github.com/bpm-crafters/process-engine-api/pull/54)
- Bump org.springframework.cloud:spring-cloud-dependencies from 2023.0.1 to 2023.0.3 [#52](https://github.com/bpm-crafters/process-engine-api/pull/52)
- Bump io.mockk:mockk-jvm from 1.13.10 to 1.13.12 [#51](https://github.com/bpm-crafters/process-engine-api/pull/51)
- Bump org.mockito.kotlin:mockito-kotlin from 5.1.0 to 5.4.0 [#50](https://github.com/bpm-crafters/process-engine-api/pull/50)
- Bump org.assertj:assertj-core from 3.24.2 to 3.26.3 [#49](https://github.com/bpm-crafters/process-engine-api/pull/49)
- Bump actions/setup-python from 4 to 5 [#46](https://github.com/bpm-crafters/process-engine-api/pull/46)
- Bump codecov/codecov-action from 1.0.2 to 4.5.0 [#45](https://github.com/bpm-crafters/process-engine-api/pull/45)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)


## [0.0.1] - 2024-08-13

### :rocket: Enhancements & Features

- If SubscribingRefreshing delivery strategy is used, make sure we don't require TaskList to complete tasks. [#41](https://github.com/bpm-crafters/process-engine-api/issues/41)
- Add initial pull support [#36](https://github.com/bpm-crafters/process-engine-api/pull/36)
- Make sure strategies are re-synced after restart [#30](https://github.com/bpm-crafters/process-engine-api/issues/30)
- Create project documentation with MkDocs [#27](https://github.com/bpm-crafters/process-engine-api/issues/27)
- Support for deployment of resources (BPMN, DMN, ...) [#26](https://github.com/bpm-crafters/process-engine-api/issues/26)
- Adopt variable-list to support "no variables" [#25](https://github.com/bpm-crafters/process-engine-api/issues/25)
- Provide camunda cockpit for C7 example [#23](https://github.com/bpm-crafters/process-engine-api/issues/23)
- Provide basic test coverage [#22](https://github.com/bpm-crafters/process-engine-api/issues/22)
- Separate user task API from service API [#21](https://github.com/bpm-crafters/process-engine-api/issues/21)
- Implement C7 remote adapter [#20](https://github.com/bpm-crafters/process-engine-api/issues/20)
- Separate Signal from Message correlation [#14](https://github.com/bpm-crafters/process-engine-api/issues/14)
- Provde Camunda 8 Adapter [#13](https://github.com/bpm-crafters/process-engine-api/issues/13)
- Support signal correlation [#11](https://github.com/bpm-crafters/process-engine-api/issues/11)
- Add new implementation job handler implementation [#10](https://github.com/bpm-crafters/process-engine-api/pull/10)
- Support event-based and job-based delivery for Embedded C7 adapter [#9](https://github.com/bpm-crafters/process-engine-api/issues/9)
- Provide basic description of the library  [#7](https://github.com/bpm-crafters/process-engine-api/issues/7)
- Create a possibility to react on task removal after delivery to a handler [#6](https://github.com/bpm-crafters/process-engine-api/issues/6)
- Provide meta information about the task (Map<String, String>) to the task handler [#5](https://github.com/bpm-crafters/process-engine-api/issues/5)
- Create a naive C7 implementation [#4](https://github.com/bpm-crafters/process-engine-api/issues/4)
- Avoid nearly engine-specific API for task subscription [#2](https://github.com/bpm-crafters/process-engine-api/issues/2)
- Create Java-based example of the API invocations. [#1](https://github.com/bpm-crafters/process-engine-api/issues/1)

### :bug: Bug Fixes

- Support list de-serialization for list types [#33](https://github.com/bpm-crafters/process-engine-api/issues/33)
- Correlation incompatibility [#15](https://github.com/bpm-crafters/process-engine-api/issues/15)

### :hammer_and_wrench: Chore

- Use latest versions [#35](https://github.com/bpm-crafters/process-engine-api/pull/35)

### :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@zambrovski](https://github.com/zambrovski)
- [@jangalinski](https://github.com/jangalinski)
- [@pschalk](https://github.com/pschalk)


