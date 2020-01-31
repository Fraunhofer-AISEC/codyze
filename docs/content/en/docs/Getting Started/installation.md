---
title: "Installation"
linkTitle: "Installation"
weight: 1
date: 2017-01-05
description: >
  Integrate Codyze into your IDE to automatically spot errors your code or use it as a console application to manually explore source code. This page explains how to install Codyze.
---


## Install Analysis Server

No matter whether you will use an IDE or the console, you first need to install the analysis server.

Prerequisites

* Java 11 or higher

Download and Install

* Download the zipped analysis server from the [release page](https://github.com/Fraunhofer-AISEC/codyze/releases)
* Unzip the file
* Run `server.bat -l` (Windows) or `server.sh -l` (Mac, Linux) from the unzipped folder to start the analysis server in LSP mode


## Install Eclipse Plugin

Codyze can be installed from the following Eclipse update site: [https://breakpoint-security.de/update](https://breakpoint-security.de/update). It has been tested against Eclipse 2019-04 and later.


1. Go to _Windows->Preferences->CPG Analysis_ and configure the path the analysis server

<img src="/img/eclipse-plugin-2.png" 
    alt="Configuring Eclipse Plugin"
    class="mt-3 mb-3 border border-info rounded">

2. If the configuration is correct, `.java` and `.cpp` files will be automatically scanned when they are saved. Any errors found by Codyze will be highlighted as problems. If Codyze verifies that an API is correctly used, it will create mark the line with a hint.

<img src="/img/eclipse-plugin-1.png" 
    alt="Configuring Eclipse Plugin"
    class="mt-3 mb-3 border border-info rounded">

## Install in Visual Studio

TODO Wolfgang