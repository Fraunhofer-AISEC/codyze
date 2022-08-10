# Codyze v2 :mag_right: :rocket:

> :warning: This is the legacy version of Codyze.
> If you are looking for a _stable_ version, please use the [2.1.1](https://github.com/Fraunhofer-AISEC/codyze/releases/tag/v2.1.1) release.

## Build & Run Codyze v2

A Java SE 11 JDK is a prerequisite. We build and test using Eclipse Temurin but any distribution should work.

To build an executable version of Codyze v2, use the `installDist` task in the project's root:

```shell
$ ./gradlew :codyze-v2:installDist
```

This will provide you with an executable Codyze installation under `codyze-v2/build/install/codyze`.
To start Codyze, change to the directory and run Codyze.


An exemplary call to start the command line interface mode would be

```shell
$ cd codyze-v2/build/install/codyze-v2
$ ./bin/codyze-v2 -m ./mark -s <sourcepath>
```
where `<sourcepath>` denotes the path to the source directory or file which should be analyzed.

Codyze can be further configured with additional command line arguments or a YAML configuration file.
Use the `-h` help option to print and see all options.
For more information about the usage and configurations, please refer to https://www.codyze.io.

## License

[Apache License 2.0](../LICENSE)
