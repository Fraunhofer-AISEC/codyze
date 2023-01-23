# Codyze :mag_right: :rocket: 

[![build](https://github.com/Fraunhofer-AISEC/codyze/actions/workflows/build.yml/badge.svg)](https://github.com/Fraunhofer-AISEC/codyze/actions/workflows/build.yml)
![GitHub last commit](https://img.shields.io/github/last-commit/Fraunhofer-AISEC/codyze)
[![codecov](https://codecov.io/gh/Fraunhofer-AISEC/codyze/branch/main/graph/badge.svg)](https://codecov.io/gh/Fraunhofer-AISEC/codyze)
![GitHub](https://img.shields.io/github/license/Fraunhofer-AISEC/codyze)
[![](https://jitpack.io/v/Fraunhofer-AISEC/codyze.svg)](https://jitpack.io/#Fraunhofer-AISEC/codyze)

> :warning: This version of Codyze is still under development.
> If you are looking for a _stable_ version, please use the [2.3.0](https://github.com/Fraunhofer-AISEC/codyze/releases/tag/v2.3.0) release.


Codyze is a static code analyzer that focuses on verifying security compliance in source code, i.e. by inferring the correct use of cryptographic libraries. It operates on code property graphs and is thus able to handle non-compiling or even incomplete code fragments.

## Build & Run Codyze
A Java SE 11 JDK is a prerequisite. We build and test using Eclipse Temurin but any distribution should work.

To build an executable version of Codyze, use the `installDist` task in the project's root:

```shell
$ ./gradlew :codyze-cli:installDist
```
This will provide you with an executable Codyze installation under `codyze-cli/build/install/codyze-cli`.

To run Codyze you can either run this executable or use the `run` task:
```shell
$ ./gradlew run
```
This will print the help message and return an error.

To actually run Codyze you must specify a subcommand:
```shell
$ ./gradlew run --args="analyze"
```
This will run the 'analyze' subcommand using the demo config file [`./codyze-cli/codyze.json`](./codyze-cli/codyze.json).

For more information, please refer to the [documentation](https://www.codyze.io).

## Research & Student Work

If you are looking for an exciting thesis project or student job in the field of static analysis, we are happy to discuss possible topics. Please contact us at _codyze [at] aisec.fraunhofer.de_.

## Support

We will continue to maintain this project for the foreseeable future on a best-effort basis. That is, if you run into any bugs or find the documentation insufficient, we encourage you to open issues or pull requests. If you are interested in support and development for commercial use, please contact us.

## License

[Apache License 2.0](./LICENSE)
