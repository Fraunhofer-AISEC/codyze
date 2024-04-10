---
title: "Configure Codyze"
linkTitle: "Configure Codyze"
no_list: true
weight: 5
date: 2022-05-24
description: >
  Codyze offers a multitude of configurations to customize the analysis to your liking.
---

There are two ways of configuring Codyze, through command line options or a configuration file.

If both are present, the command line options take precedence over the configuration file. 
For list and map type options, the data from the configuration file can be overwritten if the normal option (e.g. `--option-name`) is used. 
To append the data from the command line to the one from the configuration file, use the `additions` option (e.g. `--option-name-additions`)

## Configuration File
The configurations can also be defined with a JSON configuration file. 
Use the option `--config=<filepath>` to specify the path to the config file.
The configuration from `./codyze.json` will always be loaded if no other file is specified.

Relative paths in the configuration file are resolved relative to the configuration file location.

The configuration structure separates the options by subcommand as seen below.
```json
{
  "good-findings": false,
  "runCoko": {
    "spec": "./model.codyze.kts",
    "cokoCpg": {
      "source": "./Main.java"
    }
  }
}
```
In this example the `good-findings` argument belongs to Codyze, the `spec` argument belongs to the `runCoko` subcommand and the `source` argument belongs to the `cokoCpg` subcommand.
The value of the option is taken from the object which corresponds to the subcommand used for the execution.

It is important to note that the configuration file only sets the options for each subcommand but does not invoke the subcommand itself.
A complete usage example for using a configuration file in combination with subcommands looks as follows:
```shell
./gradlew :codyze-cli:run --args="--config=config.json runCoko cokoCpg"
```
Mind that the config file must be specified as a top-level argument before calling the respective subcommands for the `executor` or `backend`.

An exemplary configuration file can also be found in the [GitHub repository <i class="fas fa-external-link-alt"></i>](https://github.com/Fraunhofer-AISEC/codyze/blob/main/codyze-cli/config.json){target=_blank}.

## List of Configurations
This is a list of all available configurations, their descriptions and their respective name.
The names are the same for the configuration file and the CLI options.

`./` denotes the working directory in which Codyze was started.

### Codyze
| Key           | Value   | Description                                                                                                            | Default Value |
|:--------------|:--------|:-----------------------------------------------------------------------------------------------------------------------|:--------------|
| output        | Path    | The path to write the results file to.                                                                                 | `[./]`        |
| output-format | String  | Format in which the analysis results are returned.                                                                     | `sarif`       |
| good-findings | Boolean | Enable/Disable output of "positive" findings which indicate correct implementations.                                 | `true`        |
| pedantic      | Boolean | Activates pedantic analysis mode. In this mode, Codyze analyzes all given specification files and report all findings. | `false`       |

### Executors
#### Coko
| Key                 | Value    | Description                                                                                                                                                                       | Default Value     |
|:--------------------|:---------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| spec                | Path[]   | Paths to CoKo rule files.                                                                                                                                                         | `[./]`            |
| disabled-specs      | Path[]   | The specified files will be excluded from being parsed and processed.                                                                                                             | `[]`              |
| disabled-spec-rules | String[] | Rules that will be ignored by the analysis.                                                                                                                                       | `[]`              |

### Backends
#### Cpg
| Key                       | Value               | Description                                                                                                                                                                                                                                               | Default Value     |
|:--------------------------|:--------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| source                    | Path[]              | Path to the to be analyzed files or directories.                                                                                                                                                                                                          | `[./]`            |
| disabled-source           | Path[]              | Path to files or directories which should not be analyzed. Symbolic links are not followed when filtering out these paths.                                                                                                                                | `[]`              |
| additional-language       | String[]            | Specify programming languages of files to be analyzed (full names).                                                                                                                                                                                       | `[]`              |
| unity                     | boolean             | Only relevant for C++. A unity build refers to a build that consolidates all translation units into a single one, which has the advantage that header files are only processed once, adding far less duplicate nodes to the graph.                        | false             | 
| type-system-in-frontend   | boolean             | If false, the type listener system is only activated once the frontends are done building the initial AST structure. This avoids errors where the type of a node may depend on the order in which the source files have been parsed.                      | true              |
| default-passes            | boolean             | Adds all default passes in cpg (1. FilenameMapper, 2. TypeHierarchyResolver, 3. ImportResolver, 4. VariableUsageResolver, 5. CallResolver, 6. EvaluationOrderGraphPass, 7. TypeResolver).                                                                 | true              |
| passes                    | String[]            | Register these passes to be executed in the specified order. Please specify the passes with their fully qualified name.                                                                                                                                   | `[]`              |
| debug-parser              | boolean             | Enables debug output generation for the cpg parser.                                                                                                                                                                                                       | false             |
| disable-cleanup           | boolean             | Switch off cleaning up TypeManager memory after analysis, set to true only for testing.                                                                                                                                                                   | false             |
| code-in-nodes             | boolean             | Should the code of a node be shown as parameter in the node.                                                                                                                                                                                              | false             |
| annotations               | boolean             | Enables processing annotations or annotation-like elements.                                                                                                                                                                                               | false             |
| fail-on-error             | boolean             | Should parser/translation fail on parse/resolving errors (true) or try to continue in a best-effort manner (false).                                                                                                                                       | false             |
| symbols                   | Map<String, String> | Definition of additional symbols.                                                                                                                                                                                                                         | `{}`              |
| parallel-frontends        | boolean             | If true, the ASTs for the source files are parsed in parallel, but the passes afterwards will still run in a single thread. This speeds up initial parsing but makes sure that further graph enrichment algorithms remain correct.                        | false             |
| match-comments-to-nodes   | boolean             | Controls whether the CPG frontend shall use a heuristic matching of comments found in the source file to match them to the closest AST node and save it in the comment property.                                                                          | false             |
| analyze-includes          | boolean             | Enables parsing of include files. If includePaths are given, the parser will resolve symbols/templates from these in include but not load their parse tree.                                                                                               | false             |
| includes                  | Path[]              | Paths containing include files.                                                                                                                                                                                                                           | `[]`              |
| enabled-includes          | Path[]              | If includes is not empty, only the specified files will be parsed and processed in the cpg, unless it is a part of the disabled list, in which it will be ignored.                                                                                        | `[]`              |
| disabled-includes         | Path[]              | If includes is not empty, the specified includes files will be excluded from being parsed and processed in the cpg. The disabled list entries always take priority over the enabled list entries.                                                         | `[]`              |
