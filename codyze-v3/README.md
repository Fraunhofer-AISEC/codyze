# Codyze v3 :mag_right: :rocket: 

## Build & Run
Java SE 11 JDK is a prerequisite. We're using [Eclipse Temurin&trade;](https://adoptium.net/) in our build pipeline, 
but your free to choose a distribution of your liking. 

To build an executable version of Codyze v3, use the `installDist` task from the project's root directory:
```shell
$ ./gradlew :codyze-v3:codyze:installDist
```
This will provide you with an executable Codyze installation under `codyze-v3/codyze/build/install/codyze/`.

To run Codyze v3 you can either run the built executable or use the `run` task. To get an overview of the available 
command line options, use the `-h` or `--help` option as in:
```shell
$ ./gradlew :codyze-v3:codyze:run --args="--help"
```
This will print the help message and list available subcommands. You can continue to use the help on subcommands to get 
a detailed overview. 

To perform an analysis with Codyze v3 against one of our examples, use the subcommand `analyze`:
```shell
$ ./gradlew :codyze-v3:codyze:run --args="analyze"
```
This will run the 'analyze' subcommand using the demo config file (codyze-v3/codyze/codyze.json).

## License
[Apache License 2.0](../LICENSE)
