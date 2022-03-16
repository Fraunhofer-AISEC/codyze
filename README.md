# Codyze :mag_right: :rocket: 

[![build](https://github.com/Fraunhofer-AISEC/codyze/actions/workflows/build.yml/badge.svg)](https://github.com/Fraunhofer-AISEC/codyze/actions/workflows/build.yml)
![GitHub last commit](https://img.shields.io/github/last-commit/Fraunhofer-AISEC/codyze)
[![](https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=security_rating)](https://sonarcloud.io/summary/overall?id=codyze)
[![](https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=alert_status)](https://sonarcloud.io/summary/overall?id=codyze)
[![](https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=coverage)](https://sonarcloud.io/summary/overall?id=codyze)
![GitHub](https://img.shields.io/github/license/Fraunhofer-AISEC/codyze)
[![](https://jitpack.io/v/Fraunhofer-AISEC/codyze.svg)](https://jitpack.io/#Fraunhofer-AISEC/codyze)

Codyze is a static code analyzer that focuses on verifying security compliance in source code, i.e. by inferring the correct use of cryptographic libraries. It operates on code property graphs and is thus able to handle non-compiling or even incomplete code fragments.

Documentation: https://www.codyze.io

## Build & run Codyze

Java 11 (OpenJDK) is a prerequisite.

To build an executable version of Codyze, use the `installDist` task:

```shell
$ ./gradlew installDist
```

This will provide you with an executable Codyze installation under `build/install/codyze`. Change to that directory and run Codyze:

```shell
$ cd build/install/codyze
$ ./bin/codyze
```

Codyze can be configured with either command line arguments or a YAML configuration file.

For further information about the configurations, please refer to the [wiki page](https://github.com/Fraunhofer-AISEC/codyze/wiki/Usage) and https://www.codyze.io.


## Research & Student Work

If you are looking for an exciting thesis project or student job in the field of static analysis, we are happy to discuss possible topics. Please contact us at _codyze [at] aisec.fraunhofer.de_.

## Support

We will continue to maintain this project for the foreseeable future on a best-effort basis. That is, if you run into any bugs or find the documentation insufficient, we encourage you to open issues or pull requests. If you are interested in support and development for commercial use, please contact us.

## License

[Apache License 2.0](https://github.com/Fraunhofer-AISEC/codyze/blob/master/LICENSE)
