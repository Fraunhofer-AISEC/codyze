# Codyze v2 :mag_right: :rocket:

> :warning: This is the legacy version of Codyze.
> If you are looking for a _stable_ version, please use the [2.0.0-beta](https://github.com/Fraunhofer-AISEC/codyze/releases/tag/v2.0.0-beta) release.

## Build & Run Codyze v2

Java 11 (OpenJDK) is a prerequisite.

To build an executable version of Codyze v2, use the `installDist` task:

```shell
$ ./gradlew :codyze-v2:installDist
```

This will provide you with an executable Codyze installation under `build/install/codyze`.
To start Codyze, change to the directory and run Codyze.

Codyze has three execution modes:
* command line interface mode (`-c`, default)
* language server protocol mode (`-l`)
* interactive console mode (`-t`).

An exemplary call to start the commando line interface mode would be

```shell
$ cd build/install/codyze
$ ./bin/codyze -m ./mark -s <sourcepath>
```
where `<sourcepath>` denotes the path to the source directory or file which should be analyzed.

Codyze can be further configured with additional command line arguments or a YAML configuration file.
For more information about the usage and configurations, please refer to https://www.codyze.io and the corresponding [wiki page](https://github.com/Fraunhofer-AISEC/codyze/wiki/Configuring-Codyze).

## License

[Apache License 2.0](https://github.com/Fraunhofer-AISEC/codyze/blob/master/LICENSE)
