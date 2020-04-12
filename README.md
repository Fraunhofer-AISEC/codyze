<p align="center">
  <br>
  <img src="https://github.com/Fraunhofer-AISEC/codyze/workflows/Gradle%20Package/badge.svg">
  <a href="https://lgtm.com/projects/g/Fraunhofer-AISEC/codyze/context:javascript"><img alt="undefined" src="https://img.shields.io/lgtm/grade/javascript/g/Fraunhofer-AISEC/codyze.svg?logo=lgtm&logoWidth=18"/></a>
  <img src="https://img.shields.io/github/last-commit/Fraunhofer-AISEC/codyze.svg?style=popout">
  <a href="https://github.com/Fraunhofer-AISEC/codyze/blob/master/LICENSE"><img alt="undefined" src="https://img.shields.io/github/license/Fraunhofer-AISEC/codyze.svg?style=popout"></a>

  <br><br><br>
</p>

Codyze is a static code analyzer that focuses on the correct use of cryptographic libraries. It operates on code property graphs and is thus able to handle non-compiling or even incomplete code fragments.

# Build the project

Build a packaged version of Codyze:

```shell
$ ./gradlew installDist
```

This will provide you with an archive of an executable Codyze installation under `build/distributions/codyze-*.zip`. Unzip the archive and run Codyze:

```shell
$ unzip build/distributions/codyze-1.0-SNAPSHOT.zip -d /opt/
$ /opt/codyze*/codyze/bin/codyze
```

Without further command line arguments, Codyze will print its command line help:


```
Error: Missing required argument (specify one of these): (-c | -l | -t)
Usage: codyze (-c | -l | -t) [[--typestate=<NFA|WPDS>] [--interproc]] [-hV] [-m=<path>] [-o=<file>]
              [-s=<path>] [--timeout=<minutes>]
Codyze finds security flaws in source code
  -s, --source=<path>       Source file or folder to analyze.
  -m, --mark=<path>         Load MARK policy files from folder
  -o, --output=<file>       Write results to file. Use -- for stdout.
      --timeout=<minutes>   Terminate analysis after timeout
                              Default: 120
  -h, --help                Show this help message and exit.
  -V, --version             Print version information and exit.
Execution mode
  -c                        Start in command line mode.
  -l                        Start in language server protocol (LSP) mode.
  -t                        Start interactive console (Text-based User Interface).
Analysis settings
      --typestate=<NFA|WPDS>
                            Typestate analysis mode
                            NFA:  Non-deterministic finite automaton (faster, intraprocedural)
                            WPDS: Weighted pushdown system (slower, interprocedural)
      --interproc           Enables interprocedural data flow analysis (more precise but slower).
```
Please refert to http://codyze.io for further usage instructions.

### Test in IntelliJ

To debug what is sent to the LSP-Server, modify the /codyze script and make the last line sth. like:

```
echo "Starting" > /tmp/bla
echo "$JAVACMD" "$@" > /tmp/bla
exec "$JAVACMD" "$@" | tee -a /tmp/bla
```

automate with:

```
./gradlew installDist; and sed -i '$ d' build/install/codyze/bin/codyze ; and echo 'exec "$JAVACMD" "$@" | tee -a /tmp/cpgoutput' >> build/install/codyze/bin/codyze
```

To silence
```
java.lang.reflect.InaccessibleObjectException: Unable to make field protected final java.lang.reflect.Field jdk.internal.reflect.UnsafeFieldAccessorImpl.field accessible: module java.base does not "opens jdk.internal.reflect" to unnamed module @5dbd2d01
```

add `--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED` to the start-command in `build/install/codyze/bin/codyze`
