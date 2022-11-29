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
Below are short exemplary calls to start codyze in command line interface mode.
`./` refers to the top-level directory of the repository. However, for the Gradle arguments `./` refers to the directory of the project which is `codyze-cli`.

```shell
./gradlew :codyze-cli:run --args="analyze -s <sourcepath> --spec <specpath> -o <outputpath>"
```
`analyze` enters command line mode. It will parse all files given by the `-s` argument. With `--spec` you can specify the files which contain the policies you want Codyze to verify. The findings are written in SARIF format to the file given by `-o`.


## CI/CD Integration

The CLI mode is a perfect candidate for integration in CI/CD processes, such as GitHub Actions. The following file can be used as an example so set up a compliance check for Java-based applications using GitHub Actions:

```yaml
name: build

on:
  - push

env:
  CODYZE_VERSION: "2.1.1"

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: "temurin"
          java-version: "11"
      - name: Install Codyze
        run: |
          wget "https://github.com/Fraunhofer-AISEC/codyze/releases/download/v${CODYZE_VERSION}/codyze-${CODYZE_VERSION}.zip" && unzip codyze-${CODYZE_VERSION}.zip
      - name: Check compliance
        run: |
          codyze-${CODYZE_VERSION}/bin/codyze -c -o - -m codyze-${CODYZE_VERSION}/mark -s src/main/java
```

## Analysis Example

The repository contains examples which you can use to test Codyze. 
Below are the commands to call Codyze on these examples.

```shell
./gradlew :codyze-cli:run --args="analyze -s ../examples/botan/blockciphers/Prudkovskiy.Qt_LockBox/crypto.cpp --spec ../examples/botan/MARK"
```

You might notice that the Gradle task will fail.
The reason is that Codyze returns `1` if there is any negative finding and will only return `0` if no rule was violated.

The findings are located in the file `findings.sarif` in the respective project directories.
Looking into the findings, there are two passes for the rule `WrongBlockCipher`, which means that the correct algorithm `AES` was used for the block ciphers in line 16 and 22.
There are also two open findings for the rule `BadKeyLength` since the key length was not explicitly set.
The last open finding is for the rule `WrongUseOfBotan_CipherMode`.
