---
title: "Configuration"
linkTitle: "Configure Codyze"
no_list: true
weight: 2
date: 2022-05-24
description: >
  Codyze offers a multitude of configurations to customize the analysis to your liking.
---

There are two ways of configuring Codyze, through command line options or a configuration file.

If both are present, the command line options take precedence over the configuration file. 
For list and map type options, the data from the configuration file can be overwritten if the normal option (e.g. `--option-name`) is used. 
To append the data from the command line to the one from the configuration file, use the `+` option in __Codyze v2__ (e.g. `--option-name+`) or the `additions` option in __Codyze v3__ (e.g. `--option-name-additions`)


# Command Line Interface
The execution mode is specified in __Codyze v2__ through an option flag (`-c`, `-l` or `-i`).
For __Codyze v3__ subcommands are used (`analyze`, `lsp`, or `interactive`).

The help and version message can be displayed with `-h` and `-V` respectively.
The full help is only available in __Codyze v3__ if a subcommand is specified. 

# Configuration File
The configurations can also be defined with a YAML configuration file in __Codyze v2__ or a JSON configuration file in __Codyze v3__. 
Use the option `--config=<filepath>` to specify the path to the config file.

In __Codyze v2__ if `--config` is specified without an option parameter, Codyze will try to load the configuration file from `./codyze.yaml` in the working directory. 
If the option is not specified, no configuration file will be parsed.

In __Codyze v3__ the configuration from `./config.json` will always be loaded if no other file is specified.

Relative paths in the configuration file are resolved relative to the configuration file location in both versions.

For __Codyze v2__, the options are separated into different headers depending on which part of the analysis uses them.
The JSON schema located at [`schema/codyze-config-schema.json`](https://github.com/Fraunhofer-AISEC/codyze/blob/main/codyze-v2/schema/codyze-config-schema.json) can be used for generating or validating the configuration file.
Different IDE extensions for using the schema (e.g. for [VSCode](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-yaml)) can be found in this [Red Hat blog post](https://developers.redhat.com/blog/2020/11/25/how-to-configure-yaml-schema-to-make-editing-files-easier#yaml_schema).

The configuration structure for __Codyze v3__ separates the options by subcommand as seen below.
```json
{
  "analyze": {
    "source": "src"
  },
  "lsp": {
    "source": "other-src"
  }
}
```
The value of the option is taken from the object which corresponds to the subcommand used for the execution.
This means if `codyze analyze` is called, source would be `src`, and if `codyze lsp` is called, source would be `other-src`.
An exemplary configuration file for __Codyze v3__ can also be found in the [GitHub repository](https://github.com/Fraunhofer-AISEC/codyze/blob/main/codyze-v3/codyze/config.json).

# List of Configurations
This is a list of all available configurations, their descriptions and their respective name in __Codyze v2__ and __Codyze v3__.
The names are the same for the configuration file and the CLI options.

`./` denotes the working directory in which Codyze was started.

| Codyze v2               | Codyze v3               | Value               | Description                                                                                                                                                                                                                                               | Default Value     |
|:------------------------|:------------------------|:--------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| source                  | source                  | Path[]              | Path to the to be analyzed files or directories.                                                                                                                                                                                                          | `[./]`            |
| disabled-sources        | disabled-source         | Path[]              | Path to files or directories which should not be analyzed. Symbolic links are not followed when filtering out these paths.                                                                                                                                | `[]`              |
| output                  | output                  | String              | Output file in which results are written. Use "-" to print to `stdout`.                                                                                                                                                                                   | findings.sarif    | 
| timeout                 | timeout                 | long                | Terminates analysis after given minutes.                                                                                                                                                                                                                  | 120               |
| sarif                   |                         | boolean             | Controls whether the output is written in the SARIF format. __Deprecated__ in __Codyze v3__ because output is only written in SARIF format.                                                                                                               | true              |
| mark                    | spec                    | Path[]              | Paths to Mark rule files.                                                                                                                                                                                                                                 | `[./]`            |
| disabled-mark-rules     | disabled-spec           | Path[]              | The specified Mark rules will be excluded from being parsed and processed. The rule has to be specified by its fully qualified name (`package.rule`). If there is no package name, specify rule as `.rule`. Use `package.*` to disable an entire package. | `[]`              |
| no-good-findings        | no-good-findings        | boolean             | Disables output of **positive** findings.                                                                                                                                                                                                                 | false             |
| pedantic                | pedantic                | boolean             | Activates pedantic analysis mode. In this mode, Codyze analyzes all MARK rules and report all findings. This option overrides "disabled-mark-rules" and "no-good-finding" and ignores any Codyze source code comments.                                    | false             |
|                         | executor                | String              | Manually choose Executor to use with the given spec files. If unspecified, Codyze randomly selects an executor capable of evaluating the given specification files. New in __Codyze v3__.                                                                 | randomly selected |
| typestate               | typestate               | `DFA/WPDS`          | Specify typestate analysis mode.<br />`DFA`: Deterministic finite automaton (faster, intraprocedural)<br />`WPDS`: Weighted pushdown system (slower, interprocedural)                                                                                     | `DFA`             |
| additional-languages    | additional-languages    | String[]            | Specify programming languages of to be analyzed files (full names).                                                                                                                                                                                       | `[]`              |
| unity                   | unity                   | boolean             | Only relevant for C++. A unity build refers to a build that consolidates all translation units into a single one, which has the advantage that header files are only processed once, adding far less duplicate nodes to the graph.                        | false             | 
| type-system-in-frontend | type-system-in-frontend | boolean             | If false, the type listener system is only activated once the frontends are done building the initial AST structure. This avoids errors where the type of a node may depend on the order in which the source files have been parsed.                      | true              |
| default-passes          | default-passes          | boolean             | Adds all default passes in cpg (1. FilenameMapper, 2. TypeHierarchyResolver, 3. ImportResolver, 4. VariableUsageResolver, 5. CallResolver, 6. EvaluationOrderGraphPass, 7. TypeResolver).                                                                 | true              |
| passes                  | passes                  | String[]            | Register these passes to be executed in the specified order. Please specify the passes with their fully qualified name.                                                                                                                                   | `[]`              |
| debug-parser            | debug-parser            | boolean             | Enables debug output generation for the cpg parser.                                                                                                                                                                                                       | false             |
| disable-cleanup         | disable-cleanup         | boolean             | Switch off cleaning up TypeManager memory after analysis, set to true only for testing.                                                                                                                                                                   | false             |
| code-in-nodes           | code-in-nodes           | boolean             | Should the code of a node be shown as parameter in the node.                                                                                                                                                                                              | false             |
| annotations             | annotations             | boolean             | Enables processing annotations or annotation-like elements.                                                                                                                                                                                               | false             |
| fail-on-error           | fail-on-error           | boolean             | Should parser/translation fail on parse/resolving errors (true) or try to continue in a best-effort manner (false).                                                                                                                                       | false             |
| symbols                 | symbols                 | Map<String, String> | Definition of additional symbols.                                                                                                                                                                                                                         | `{}`              |
| parallel-frontends      | parallel-frontends      | boolean             | If true, the ASTs for the source files are parsed in parallel, but the passes afterwards will still run in a single thread. This speeds up initial parsing but makes sure that further graph enrichment algorithms remain correct.                        | false             |
|                         | match-comments-to-nodes | boolean             | Controls whether the CPG frontend shall use a heuristic matching of comments found in the source file to match them to the closest AST node and save it in the comment property. New in __Codyze v3__.                                                    | false             |
| analyze-includes        | analyze-includes        | boolean             | Enables parsing of include files. If includePaths are given, the parser will resolve symbols/templates from these in include but not load their parse tree.                                                                                               | false             |
| includes                | includes                | Path[]              | Paths containing include files.                                                                                                                                                                                                                           | `[]`              |
| enabled-includes        | enabled-includes        | Path[]              | If includes is not empty, only the specified files will be parsed and processed in the cpg, unless it is a part of the disabled list, in which it will be ignored.                                                                                        | `[]`              |
| disabled-includes       | disabled-includes       | Path[]              | If includes is not empty, the specified includes files will be excluded from being parsed and processed in the cpg. The disabled list entries always take priority over the enabled list entries.                                                         | `[]`              |
 
