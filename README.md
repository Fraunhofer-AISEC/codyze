<p align="center">
  <br>
  <img src="https://github.com/Fraunhofer-AISEC/codyze/workflows/build/badge.svg">
  <img src="https://img.shields.io/github/last-commit/Fraunhofer-AISEC/codyze.svg?style=popout">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=security_rating">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=alert_status">
  <a href="https://github.com/Fraunhofer-AISEC/codyze/blob/master/LICENSE"><img alt="undefined" src="https://img.shields.io/github/license/Fraunhofer-AISEC/codyze.svg?style=popout"></a>
  <br><br><br>
</p>

Codyze is a static code analyzer that focuses on the correct use of cryptographic libraries. It operates on code property graphs and is thus able to handle non-compiling or even incomplete code fragments.

Documentation: https://www.codyze.io

# Build & run Codyze

Java 11 (OpenSDK) is a prerequisite.

To build an executable version of Codyze, use the `installDist` task:

```shell
$ ./gradlew installDist
```

This will provide you with an executable Codyze installation under `build/install/codyze`. Change to that directory and run Codyze:

```shell
$ cd build/install/codyze
$ ./bin/codyze
```

Without further command line arguments, Codyze will print its command line help:


```
Error: Missing required argument (specify one of these): (-c | -l | -t)
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
Please refer to https://www.codyze.io for further usage instructions.

# Research & Student Work

If you are looking for an exciting thesis project or student job in the field of static analysis, we are happy to discuss possible topics.

# Support

We will continue to maintain this project for the foreeable future on a best-effort basis. That is, if you run into any bugs or find the documentation insufficient, we encourage you to open issues or pull requests. If you are interested in support and development for commercial use, please contact us.

# License

[Apache License 2.0](https://github.com/Fraunhofer-AISEC/codyze/blob/master/LICENSE)
