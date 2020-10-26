---
title: "Install Codyze"
linkTitle: "Install Codyze"
no_list: true
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
* Run `bin\codyze.bat -l` (Windows) or `bin/codyze -l` (Mac, Linux) from the unzipped folder to start the analysis server in LSP mode


## Integration in Eclipse

Codyze can be installed from an Eclipse update site. It has been tested against Eclipse 2019-12 and later.

1. In Eclipse, click on `Help`->`Install New Software...`
2. Click `Add...` to add a new update site
3. Choose a name and enter the location [https://codyze.s3.eu-central-1.amazonaws.com/codyze-eclipse-plugin/](https://codyze.s3.eu-central-1.amazonaws.com/codyze-eclipse-plugin/) (note this is an Eclipse update site URL and not suited to open with a web browser)

<img src="/img/eclipse-update-site.png" 
    alt="Adding Eclipse Update Site"
    class="mt-3 mb-3 border border-info rounded">

4. Choose and install `Codyze Code Analyzer`

<img src="/img/eclipse-plugin-installation.png" 
    alt="Adding Eclipse Update Site"
    class="mt-3 mb-3 border border-info rounded">

Once installed, configure the Eclipse plugin to use the local LSP server:

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
3. Add a new server definition of type `Executable` for extension `java` and navigate to your local `codyze-<version>/bin/codyze` script. 
 
<img src="/img/lsp-settings-intellij.png" 
alt="IntelliJ LSP Settings" 
class="mt-3 mb-3 border border-info rounded">

If everything works as intended, you should see a green circle in your IntelliJ status bar, indicating that the connection to the language server was successful.

## Integration in Visual Studio 2019

The Codyze eclipse can be installed from the Visual Studio 2019 Marketplace

<img src="/img/vs-plugin.jpg" 
alt="Installation from Visual Studio Market" 
class="mt-3 mb-3 border border-info rounded">

If you prefer installing the plugin from the release page, proceed as follows:

1. Download the zipped extension from the [release page](https://github.com/Fraunhofer-AISEC/codyze-vs-plugin/releases)
2. Unzip the file
3. Double-click the VSIX-file to install the extension
4. Launch Visual Studio

On startup, the plugin will ask you for the path to Codyze and to the mark files you want to use. If everythings checks out, the plugin will automatically start an instance of Codyze when a solution is opened. It will then scan `.cpp` files when opened or saved and highlight potential problems.

If you later want to adjust the path to Codyze, the mark files or change the command line arguments used for Codyze, in Visual Studio goto `Tools` -> `Options...` -> `Codyze Plugin` -> `Codyze Settings`.
