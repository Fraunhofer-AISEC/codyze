---
title: "Using the Codyze Console"
linkTitle: "Using the Codyze Console"
no_list: true
weight: 3
date: 2020-05-25
expiryDate: 2022-05-24
description: >
  The Codyze console allows to interactively explore and analyze source code.
---


## Starting in Console Mode

When starting Codyze with the `-t` option in __Codyze v2__ or with the `console` subcommand in __Codyze v3__, it will start an interactive Python console which you can use to explore your source code projects. 

```shell
$ codyze -t

      ██████╗ ██████╗ ██████╗ ██╗   ██╗███████╗███████╗
     ██╔════╝██╔═══██╗██╔══██╗╚██╗ ██╔╝╚══███╔╝██╔════╝
     ██║     ██║   ██║██║  ██║ ╚████╔╝   ███╔╝ █████╗  
     ██║     ██║   ██║██║  ██║  ╚██╔╝   ███╔╝  ██╔══╝  
     ╚██████╗╚██████╔╝██████╔╝   ██║   ███████╗███████╗
      ╚═════╝ ╚═════╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝
     
                     Welcome to Codyze!
                To get help, enter "help()"

>>>
```

The console is a standard Python console and offers a few extra commands to load, analyze, and explore source code with Codyze. It supports the following standard keyboard shortcuts:

| Object                                                       | Shorthand                                 |
|--------------------------------------------------------------|-------------------------------------------|
| <kbd>CTRL</kbd>+<kbd>C</kbd> or <kbd>CTRL</kbd>+<kbd>D</kbd> | Leaves the console                        |
| <kbd>UP</kbd>, <kbd>DOWN</kbd>                               | Navigate backand forth in command history |
| <kbd>CTRL</kbd>+<kbd>R</kbd>                                 | Search in command history                 |
| <kbd>ALT</kbd>+<kbd>B</kbd>                                  | Go back one word                          |
| <kbd>ALT</kbd>+<kbd>F</kbd>                                  | Go forth one word                         |


To run any Python snippet, simply enter it at the command prompt:

```python
>>> print('Hello world')
Hello world
```

To interact with Codyze, the console provides three main objects: `server`, `query` and `graph`. Each object has a shorthand notation that is an alias to save a few keystrokes, so instead of `server` you may simply type `s`.


| Object | Shorthand |
|--------|-----------|
| server | s         |
| query  | q         |
| graph  | g         |


## `server`

The `server` object controls the Codyze analysis server. Use it to load source code projects, MARK files, and start the analysis. 

| Command              | Description                                                                                                                                                                                                    |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `s.analyze(path)`    | Starts the analysis for the given folder or source file.                                                                                                                                                       |
| `s.show_findings()`  | List all findings, including correct and incorrect uses of MARK rules. Make sure to call `analyze` before. Note that the line numbers displayed by `show_findings()` start counting at 1, as your editor does. |
| `s.list_rules()`     | List all MARK rules which are currently active.                                                                                                                                                                |
| `s.load_rules(path)` | Load additional MARK rules from the given folder or file. Make sure to load rules before calling `analyze`                                                                                                     |


## `query`

The `query` object provides access to the Crymlin query interface. Crymlin is an extension of the Apache Gremlin graph traversal language and comes with various shortcuts for exploring code property graphs. To get a first impression of Crymlin, consider the following snippet:

```python
>>> q.methods()
     .name()
     .toList()
[main, toString, myMethod, someOtherMethod]
```

This snippet uses the query object `q` to get all methods (in all files and all classes) from the graph, retrieves their names (`name`) and collect them in a list (`toList`).