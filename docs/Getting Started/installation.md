---
title: "Install Codyze"
linkTitle: "Install Codyze"
no_list: true
weight: 1
date: 2017-01-05
description: >
  Integrate Codyze into your IDE to automatically spot errors in your code or use it as a console application to manually explore source code. This page explains how to install Codyze.
---

## Prerequisites
* Java 11 or higher


## Build from Source
Clone the source code for Codyze from the [project's GitHub repository <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze).

### Codyze v2
##### Executable Version
* To build an executable version, run `./gradlew :codyze-v2:installDist`
* The executable Codyze installation is located under `codyze-v2/build/install/codyze-v2`
##### Gradle Run Task
You can also use the Gradle run task `./gradlew :codyze-v2:run` to directly run Codyze.
This will print the help message and return an error.

Arguments can be passed with the `--args` option. 

### Codyze v3
##### Executable Version
* To build an executable version, run `./gradlew :codyze-v3:codyze:installDist`
* The executable Codyze installation is located under `codyze-v3/codyze/build/install/codyze`
##### Gradle Run Task
You can also use the Gradle run task `./gradlew :codyze-v3:codyze:run` to directly run Codyze.
This will print the help message and return an error.

Arguments can be passed with the `--args` option.


## Pre-built Release
!!! note

    All following example calls in this documentation will assume that the source code was cloned and use the exact file structure.
    If you want to test Codyze with these calls, please clone the repository.

* Download a zipped release of Codyze from our [GitHub release page <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze/releases)
* Unzip the file
* Execute Codyze using `bin\codyze.bat` (Windows) or `bin/codyze` (Mac, Linux)


## Docker
We're also offering Codyze as a container image. You can find an image with the latest release in the [project's container registry <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze/pkgs/container/codyze).
