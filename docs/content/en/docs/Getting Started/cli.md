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
Usage: codyze (-c | -l | -t) [[--typestate=<NFA|WPDS>] [--interproc]] [-hV] [-m=<path>] [-o=<file>]
              [-s=<path>] [--timeout=<minutes>]
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
                            NFA:  Non-deterministic finite automaton (faster, intraprocedural)
                            WPDS: Weighted pushdown system (slower, interprocedural)
      --interproc           Enables interprocedural data flow analysis (more precise but slower).

```

`-c` enters command line mode. It will parse all files given by the `-s` argument, analyze them against the MARK policies given by `-m`, and write the findings in JSON format to the file given by `-o`. If `--` is given as the output name, the results will be dumped to stdout.

Note that line numbers of findings in JSON output start by 0 and are thus off by one compared to the `server.show_findings()` command in the interactive console.