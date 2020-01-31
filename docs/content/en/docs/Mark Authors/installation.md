---
title: "Install MARK IDE"
linkTitle: "Install MARK IDE"
weight: 1
date: 2019-12-10
description: >
  Setting up the MARK authoring environment with Eclipse XText
---

MARK is a simple domain specific language (DSL) that can be written in any text editor. We recommend however using the MARK IDE which brings syntax highlighting, auto-completion, and some other benefits that will support you in writing MARK rules.

You may choose from any of these ways to use MARK authoring support:

1. A standalone Eclipse-based IDE
1. An Eclipse plugin
1. A language server (LSP) that can be integrated into VS Code, IntelliJ, and other IDEs

## Standalone Eclipse IDE

* Download the standalone Eclipse IDE from TODO. We provide versions for Windows, Mac, and Linux (each 64 bit).

## Eclipse Plugin

* In Eclipse, click on `Help`->`Install new software`
* Add the update site: `https://update.breakpoint-security.de`
* Install (TODO: Name of product in eclipse update site)

## Language Server

* Download the language server as cross-platform Jar file from TODO
* Start the language server using the command `java -jar TODO.jar -lsp`
* In the IDE of your choice, install an LSP plugin if needed and connect to your language server. TODO