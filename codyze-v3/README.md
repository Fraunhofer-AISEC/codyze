# Codyze v3 :mag_right: :rocket:

> :warning: This version of Codyze is still under development.
> If you are looking for a _stable_ version, please use the [2.1.1](https://github.com/Fraunhofer-AISEC/codyze/releases/tag/v2.1.1) release.

## Project Structure
Codyze v3 is set up as a gradle multi project build.

[`codyze-core`](https://github.com/Fraunhofer-AISEC/codyze/tree/main/codyze-v3/codyze-core) contains the core functionalities of Codyze. 
If you want to use Codyze as a library in your own project, we suggest using this.

[`codze-specification-languages`](https://github.com/Fraunhofer-AISEC/codyze/tree/main/codyze-v3/codze-specification-languages) contains all built-in specification languages which Codyze can verify.

[`codyze`](https://github.com/Fraunhofer-AISEC/codyze/tree/main/codyze-v3/codyze) combines all projects into an executable version of Codyze and adds a command line interface.

## Build & Run Codyze v3
A Java SE 11 JDK is a prerequisite. We build and test using Eclipse Temurin but any distribution should work.

To build an executable version of Codyze v3, use the `installDist` task in the project's root:

```shell
$ ./gradlew :codyze-v3:codyze:installDist
```
This will provide you with an executable Codyze installation under `codyze-v3/codyze/build/install/codyze`.

To run Codyze v3 you can either run this executable or use the `run` task:
```shell
$ ./gradlew :codyze-v3:codyze:run
```
This will print the help message and return an error.

To actually run Codyze v3 you must specify a subcommand:
```shell
$ ./gradlew :codyze-v3:codyze:run --args="analyze"
```
This will run the 'analyze' subcommand using the demo config file [`./codyze/codyze.json`](./codyze/codyze.json).

For more information, please refer to the [documentation](https://www.codyze.io).

## License

[Apache License 2.0](../LICENSE)
