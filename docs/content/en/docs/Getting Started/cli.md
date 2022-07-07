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

When running in command line interface (CLI) mode, Codyze can be used to automatically check a code base against a set of MARK rules.
A short exemplary call to start codyze in command line interface mode would be

### Codyze v2
```r
$ ./bin/codyze-v2 -c -s <sourcepath> -m ./mark -o <outputpath>
```

`-c` enters command line mode. It will parse all files given by the `-s` argument, analyze them against the MARK policies given by `-m`, and write the findings in JSON format to the file given by `-o`. If `-` is given as the output name, the results will be dumped to stdout.

### Codyze v3
```r
$ ./bin/codyze analyze -s <sourcepath> --spec ./mark -o <outputpath>
```

Note that line numbers of findings in JSON output start by 0.


## CI/CD Integration

The CLI mode is a perfect candidate for integration in CI/CD processes, such as GitHub Actions. The following file can be used as an example so set up a compliance check for Java-based applications using GitHub Actions:

```yaml
name: build

on:
  - push

env:
  CODYZE_VERSION: "2.0.0"

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: "zulu"
          java-version: "11"
      - name: Install codyze
        run: |
          wget "https://github.com/Fraunhofer-AISEC/codyze/releases/download/v${CODYZE_VERSION}/codyze-${CODYZE_VERSION}.zip" && unzip codyze-${CODYZE_VERSION}.zip
      - name: Check compliance
        run: |
          codyze-${CODYZE_VERSION}/bin/codyze -c -o - -m codyze-${CODYZE_VERSION}/mark -s src/main/java
```
