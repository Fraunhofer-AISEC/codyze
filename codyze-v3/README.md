# Codyze v3 :mag_right: :rocket: 

## Build & Run Codyze
Java 11 (OpenJDK) is a prerequisite.

To build an executable version of Codyze, use the `installDist` task:

```shell
$ ./gradlew :codyze-v3:installDist
```
This will provide you with an executable Codyze installation under `codyze-v3/build/install/codyze`.

To run codyze-v3 you can either run this executable or use the `run` task:
```shell
$ ./gradlew :codyze-v3:codyze:run
```
This will print the help message and return an error.

To actually run codyze-v3 you must specify a subcommand:
```shell
$ ./gradlew :codyze-v3:codyze:run --args="analyze"
```
This will run the 'analyze' subcommand using the demo config file (codyze-v3/codyze/codyze.json).

## License

[Apache License 2.0](https://github.com/Fraunhofer-AISEC/codyze/blob/master/LICENSE)
