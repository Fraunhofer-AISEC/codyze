---
title: "Install Codyze"
linkTitle: "Install Codyze"
no_list: true
weight: 1
date: 2017-01-05
description: >
  Integrate Codyze into your IDE to automatically spot errors in your code or use it as a console application to manually explore source code. This page explains how to install Codyze.
---


No matter whether you will use an IDE or the console, you first need to install the analysis server.

Prerequisites

* Java 11 or higher


## Release (from GitHub)
* Download the zipped analysis server from the [release page](https://github.com/Fraunhofer-AISEC/codyze/releases)
* Unzip the file
* Codyze is located at `bin\codyze.bat` (Windows) or `bin/codyze` (Mac, Linux) in the unzipped folder

## Docker
* ?

## Build from Source
* Clone the source code for Codyze from https://github.com/Fraunhofer-AISEC/codyze.git
* To build an executable version of Codyze, run `./gradlew installDist`
* The executable Codyze installation is located under `build/install/codyze`