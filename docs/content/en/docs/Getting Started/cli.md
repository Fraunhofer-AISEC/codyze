---
title: "Using Codyze from the console"
linkTitle: "using"
weight: 2
date: 2020-01-30
description: >
  Codyze is a search engine to find vulnerabilities in source code. Explore your code base manually by using the Codyze command line tool.
---


## Command line mode

When running Codyze in command line interface (CLI) mode, it can be used to automatically check a code base against a set of MARK rules. Simply run `codyze` to see the supported command line flags.

```bash
Usage: codyze (-c | -l | -t) [-hiV] [-m=<path>] [-o=<file>] [-s=<path>]
              [--timeout=<minutes>]
Codyze finds security flaws in source code
      --interproc           Enables interprocedural analysis (more precise but
                              slower).
  -s, --source=<path>       Source file or folder to analyze.
  -m, --mark=<path>         Load MARK policy files from folder
  -o, --output=<file>       Write results to file. Use -- for stdout.
      --timeout=<minutes>   Terminate analysis after timeout
                              Default: 120
      --typestate=
  -h, --help                Show this help message and exit.
  -V, --version             Print version information and exit.
Execution mode
  -c                        Start in command line mode.
  -l                        Start in language server protocol (LSP) mode.
  -t                        Start interactive console (Text-based User
                              Interface).
```