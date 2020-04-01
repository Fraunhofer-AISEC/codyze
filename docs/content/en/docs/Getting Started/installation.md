---
title: "Install Codyze"
linkTitle: "Install Codyze"
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


## Integration in Eclipse

Codyze can be installed from the following Eclipse update site: [https://update.breakpoint-security.com](https://update.breakpoint-security.com). It has been tested against Eclipse 2019-04 and later.


1. Go to _Windows->Preferences->Codyze Code Analysis_ and configure the path to the analysis server binary

<img src="/img/eclipse-plugin-2.png" 
    alt="Configuring Eclipse Plugin"
    class="mt-3 mb-3 border border-info rounded">

2. If the configuration is correct, `.java` and `.cpp` files will be automatically scanned when they are saved. Any errors found by Codyze will be highlighted as problems. If Codyze verifies that an API is correctly used, it will create mark the line with a hint.

<img src="/img/eclipse-plugin-1.png" 
    alt="Configuring Eclipse Plugin"
    class="mt-3 mb-3 border border-info rounded">

## Integration in IntelliJ

1. Download and install the `LSP Support` plugin. Restart IntelliJ.
2. Goto `Settings` -> `Language Server Protocol` -> `Server Definitions`
3. Add a new server definition of type `Executable` for extension `java` and navigate to your local `/opt/codyze/codyze-1.0.0/bin/codyze` script. 
 
![](lsp-settings-intellij.png "IntelliJ LSP Settings")

If everything works as intended, you should see a green circle in your IntelliJ status bar, indicating that the connection to the language server was successful. Afterwards each time you open a Java file, it should get translated into the neo4j graph, visible in the neo4j browser (http://localhost:7474/browser).

## Integration in Visual Studio 2019

1. Download the zipped extension on the [release page](https://github.com/Fraunhofer-AISEC/codyze-vs-plugin/releases)
2. Unzip the file
3. Double-click the VSIX-file to install the extension
4. Launch Visual Studio

On startup, the plugin will ask you for the path to Codyze and to the mark files you want to use. If everythings checks out, the plugin will automatically start an instance of Codyze when a solution is opened. It will then scan `.cpp` files when opened or saved and highlight potential problems.

If you later want to adjust the path to Codyze, the mark files or change the command line arguments used for Codyze, in Visual Studio goto `Tools` -> `Options...` -> `Codyze Plugin` -> `Codyze Settings`.
