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

When running in command line interface (CLI) mode, Codyze can be used to automatically check a code base against a set of MARK rules. Run `codyze` to see the supported command line flags:

```r
Usage: codyze (-c | -l | -t) [[--typestate=<NFA|WPDS>]] [[--analyze-includes]
              [--includes=<includesPath>[:|;<includesPath>...]] [--includes=<includesPath>[:|;
              <includesPath>...]]...] [-hV] [--no-good-findings] [-m=<path>] [-o=<file>]
              [-s=<path>] [--timeout=<minutes>]
Codyze finds security flaws in source code
  -s, --source=<path>       Source file or folder to analyze.
  -m, --mark=<path>         Load MARK policy files from folder
  -o, --output=<file>       Write results to file. Use - for stdout.
      --timeout=<minutes>   Terminate analysis after timeout
                              Default: 120
      --no-good-findings    Disable output of "positive" findings which indicate correct
                              implementations
  -h, --help                Show this help message and exit.
  -V, --version             Print version information and exit.
Execution mode
  -c                        Start in command line mode.
  -l                        Start in language server protocol (LSP) mode.
  -t                        Start interactive console (Text-based User Interface).
Analysis settings
      --typestate=<NFA|WPDS>
                            Typestate analysis mode
                            NFA:  Non-deterministic finite automaton (faster, intraprocedural)
                            WPDS: Weighted pushdown system (slower, interprocedural)
Translation settings
      --analyze-includes    Enables parsing of include files. By default, if --includes are given,
                              the parser will resolve symbols/templates from these include, but not
                              load their parse tree.
      --includes=<includesPath>[:|;<includesPath>...]
                            Path(s) containing include files. Path must be separated by :
                              (Mac/Linux) or ; (Windows)
```

`-c` enters command line mode. It will parse all files given by the `-s` argument, analyze them against the MARK policies given by `-m`, and write the findings in JSON format to the file given by `-o`. If `-` is given as the output name, the results will be dumped to stdout.

Note that line numbers of findings in JSON output start by 0 and are thus off by one compared to the `server.show_findings()` command in the interactive console.

## CI/CD Integration

The CLI mode is a perfect candidate for integration in CI/CD processes, such as GitHub Actions. The following file can be used as an example so set up a compliance check for Java-based applications using GitHub Actions:

```yaml
name: build

on:
  - push

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v1
        with:
          java-version: "11"
      - name: Install codyze
        run: |
          export CODYZE_VERSION=1.4.1
          wget https://github.com/Fraunhofer-AISEC/codyze/releases/download/v${CODYZE_VERSION}/codyze-${CODYZE_VERSION}.zip && unzip codyze-${CODYZE_VERSION}.zip
      - name: Check compliance
        run: |
          export CODYZE_VERSION=1.4.1
          codyze-${CODYZE_VERSION}/bin/codyze -c -o - -m codyze-${CODYZE_VERSION}/mark -s src/main/java
```
