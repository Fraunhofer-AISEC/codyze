---
title: "Build the project"
linkTitle: "Build the project"
weight: 1
date: 2020-01-30
description: >
  Set up your development environment and build Codyze from the sources.
---

## Prerequisites

* Java (OpenJDK) 11 or later

## Check out the source code

TODO

## Build the project

`./gradlew clean spotlessApply build publishToMavenLocal installDist`

The purpose of these gradle tasks is as follows:

* _clean_ Removes artifacts from previous build
* _spotlessApply_ Applies source code formatting. Make sure to always format the code with the settings in `formatter-settings.xml` - otherwise the build server will reject it.
* _build_ Builds the main artifact (the jar file containing Codyze)
* _publishToMavenLocal_ optional
* _installDist_ Create an executable script for Linux, Mac, and Windows in `build/install/codyze`

## Run Codyze

Run `build/install/codyze/bin/codyze`

You should get the usage help message of Codyze:

```
Usage: codyze (-c | -l | -t) [[--typestate=<NFA|WPDS>] [--interproc]] [-hV] [-m=<path>] [-o=<file>] [-s=<path>] [--timeout=<minutes>]
Codyze finds security flaws in source code
  -s, --source=<path>       Source file or folder to analyze.
  -m, --mark=<path>         Load MARK policy files from folder
  -o, --output=<file>       Write results to file. Use -- for stdout.
      --timeout=<minutes>   Terminate analysis after timeout
                              Default: 120
  -h, --help                Show this help message and exit.
  -V, --version             Print version information and exit.
Execution mode
  -c                        Start in command line mode.
  -l                        Start in language server protocol (LSP) mode.
  -t                        Start interactive console (Text-based User Interface).
Analysis settings
      --typestate=<NFA|WPDS>
                            Typestate analysis mode
                            NFA:  Use non-deterministic finite automaton
                            WPDS: Use weighted pushdown system
      --interproc           Enables interprocedural analysis (more precise but slower).
```

