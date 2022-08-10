# Codyze :mag_right: :rocket: 

[![build](https://github.com/Fraunhofer-AISEC/codyze/actions/workflows/build.yml/badge.svg)](https://github.com/Fraunhofer-AISEC/codyze/actions/workflows/build.yml)
![GitHub last commit](https://img.shields.io/github/last-commit/Fraunhofer-AISEC/codyze)
[![](https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=security_rating)](https://sonarcloud.io/summary/overall?id=codyze)
[![](https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=alert_status)](https://sonarcloud.io/summary/overall?id=codyze)
[![](https://sonarcloud.io/api/project_badges/measure?project=codyze&metric=coverage)](https://sonarcloud.io/summary/overall?id=codyze)
![GitHub](https://img.shields.io/github/license/Fraunhofer-AISEC/codyze)
[![](https://jitpack.io/v/Fraunhofer-AISEC/codyze.svg)](https://jitpack.io/#Fraunhofer-AISEC/codyze)

> :warning: Note: We are currently redesigning Codyze. We have moved most of the functionality into a subpackage `codyze-v2`. For the foreseeable future, we continue to maintain the legacy version of Codyze.
> 
> Gradually, we are replacing legacy functionality with the redesigned one. Where this approach isn't feasible due to breaking changes, we're going to offer a switch to either use the legacy version or redesigned version.
>
> If you are looking for a _stable_ version, please use the [2.0.0-beta](https://github.com/Fraunhofer-AISEC/codyze/releases/tag/v2.0.0-beta) release.


Codyze is a static code analyzer that focuses on verifying security compliance in source code, i.e. by inferring the correct use of cryptographic libraries. It operates on code property graphs and is thus able to handle non-compiling or even incomplete code fragments.

Codyze has three execution modes:

* __Analyze mode__ checks the source code against a set of rules. This mode can be integrated into scripts and automated build processes.
* __Language Server Protocol mode__ integrates Codyze into an IDE and automatically analyzes code while developing.
* __Interactive Console mode__ allows to explore and analyze the source code interactively.



## Build & Run Codyze

This repository contains two versions of Codyze, Codyze v2 and Codyze v3. It is set up as a separate composite build so both versions can be built separately.
Please refer to the READMEs in the [`codyze-v2`](https://github.com/Fraunhofer-AISEC/codyze/tree/main/codyze-v2) and [`codyze-v3`](https://github.com/Fraunhofer-AISEC/codyze/tree/main/codyze-v3) directories for the concrete build instructions.


## Documentation
The full documentation can be found at https://www.codyze.io.


## Research & Student Work

If you are looking for an exciting thesis project or student job in the field of static analysis, we are happy to discuss possible topics. Please contact us at _codyze [at] aisec.fraunhofer.de_.

## Support

We will continue to maintain this project for the foreseeable future on a best-effort basis. That is, if you run into any bugs or find the documentation insufficient, we encourage you to open issues or pull requests. If you are interested in support and development for commercial use, please contact us.

## License

[Apache License 2.0](./LICENSE)
