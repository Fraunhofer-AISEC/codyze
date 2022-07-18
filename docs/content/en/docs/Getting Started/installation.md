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


## Pre-built Release
* Download a zipped release of Codyze from our [GitHub release page <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze/releases)
* Unzip the file
* Execute Codyze using `bin\codyze.bat` (Windows) or `bin/codyze` (Mac, Linux)

## Docker
We're also offering Codyze as a container image. You can find an image with the latest release in the [project's container registry <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze/pkgs/container/codyze).

<!-- TODO: add description of container -->

## Build from Source
* Clone the source code for Codyze from the [project's GitHub repository <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze).
* To build an executable version of Codyze, run `./gradlew installDist`
* The executable Codyze installation is located under `build/install/codyze`
