---
title: "Using CLI mode"
linkTitle: "Using CLI mode"
no_list: true
weight: 2
date: 2020-01-30
description: >
  In CLI mode, Codyze integrates into scripts and automated build processes.
---


## Command line mode

When running in command line interface (CLI) mode, Codyze can be used to automatically check a code base against a set of rules given in a supported specification language like Coko.
Below are short exemplary calls to start codyze in command line interface mode.
`./` refers to the top-level directory of the repository. However, for the Gradle arguments `./` refers to the directory of the project, which is `codyze-cli`.

```shell
./gradlew :codyze-cli:run --args="<executor> --spec <specpath> <backend> -s <sourcepath>"
```
Because Codyze is built to be modular and support many specification languages as well as code analysis backends, there are subcommands to select the `executor`/`backend`.
To find what arguments each `executor`/`backend` accept, use the `--help` argument:

To show the available `executors` use:
```shell
./gradlew :codyze-cli:run --args="--help"
```

To show the arguments accepted by an executor and the available `backend`s use:
```shell
./gradlew :codyze-cli:run --args="<executor> --help"
```

To show the arguments accepted by a `backend` use:
```shell
./gradlew :codyze-cli:run --args="<executor> <backend> --help"
```

## Analysis Example

The repository contains examples which you can use to test Codyze.
Below are the commands to call Codyze on these examples.

```shell
./gradlew :codyze-cli:run --args="runCoko --spec ../codyze-specification-languages/coko/coko-dsl/src/test/resources/model.codyze.kts --spec ../codyze-specification-languages/coko/coko-dsl/src/test/resources/javaimpl.codyze.kts cokoCpg -s ../codyze-specification-languages/coko/coko-dsl/src/test/resources/java/Main.java" 
```

This configures `Codyze` to use the 'coko' executor and the 'cokoCpg' backend.
You will see the result printed to the console and a `findings.sarif` files is generated in the `codyze-cli` folder.
The spec files contain a single rule, which checks that every change to a database is logged.
The sample Java file adheres to the rule, so there should be no issues in the result.


## CI/CD Integration

The CLI mode is a perfect candidate for integration in CI/CD processes, such as GitHub Actions. The following file can be used as an example so set up a compliance check for Java-based applications using GitHub Actions:

```yaml
name: build

on:
  - push

env:
  CODYZE_VERSION: "2.1.1"

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: "temurin"
          java-version: "17"
      - name: Install Codyze
        run: |
          wget "https://github.com/Fraunhofer-AISEC/codyze/releases/download/v${CODYZE_VERSION}/codyze-${CODYZE_VERSION}.zip" && unzip codyze-${CODYZE_VERSION}.zip
      - name: Check compliance
        run: |
          codyze-${CODYZE_VERSION}/bin/codyze <arguments>
```
