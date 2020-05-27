---
title: "Using the Codyze Console"
linkTitle: "Using the Codyze Console"
no_list: true
weight: 3
date: 2020-05-25
description: >
  The Codyze console allows to interactively explore and analye source code.
---


## Starting in Console Mode

When starting Codyze with the `-t` option, it will start an interactive Python console that you can use to explore your source code projects. 

```r
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

The console is a standard python console and offers a few extra commands to load, analyze, and explore source code with Codyze. It supports the following standard keyboard shortcuts:

| Object | Shorthand |
|---|---|
| <kbd>CTRL</kbd>+<kbd>C</kbd> or <kbd>CTRL</kbd>+<kbd>D</kbd> | Leaves the console |
| <kbd>UP</kbd>, <kbd>DOWN</kbd> | Navigate backand forth in command history |
| <kbd>CTRL</kbd>+<kbd>R</kbd> | Search in command history |
| <kbd>ALT</kbd>+<kbd>B</kbd> | Go back one word |
| <kbd>ALT</kbd>+<kbd>F</kbd> | Go forth one word |


To run any Python snippet, simply enter it at the command prompt:

```python
>>> print('Hello world')
Hello world
```

To interact with Codyze, the console provides three main objects: `server`, `query` and `graph`. Each object has a shorthand notation that is an alias to save a few keystrokes, so instead of `server` you may simply type `s`.


| Object | Shorthand |
|---|---|
| server  | s  |
| query   | q  |
| graph   | g  |


## `server`

The `server` object controls the Codyze analysis server. Use it to load source code projects, MARK files, and start the analysis. 

| Command | Description |
|---|---|
|  `s.analyze(path)`  | Starts the analysis for the given folder or source file. |
|  `s.show_findings()`  | List all findings, including correct and incorrect uses of cryptography. Make sure to call `analyze` before. |
|  `s.list_rules()`  | List all MARK rules which are currently active. |
|  `s.load_rules(path)`  | Load additional MARK rules from the given folder or file. Make sure to load rules before calling `analyze` |


## `query`

The `query` object provides access to the Crymlin query interface. Crymlin is an extension of the Apache Gremlin graph traversal language and comes with various shortcuts for exploring code property graphs. To get a first impression of Crymlin, consider the following snippet:

```python
>>> q.methods()
     .name()
     .toList()
[main, toString, myMethod, someOtherMethod]
```

This snippet uses the query object `q` to get all methods (in all files and all classes) from the graph, retrieves their names (`name`) and collect them in a list (`toList`).

In this example, `methods()` and `name()` are so-called *traversal steps* and `toList()` is a *terminator*. Every graph traversal needs a terminator to produce meaningful output. Until the console encounters a terminator, it will treat the graph traversal as an iterator that can be modified by further traversal steps.

### Query terminators

The most used terminators are:

| Terminator | Description |
|---|---|
|  `.next()`  | Returns the first element that matches the query or `NoSuchElementException` if the query result is empty. |
|  `.toList()`  | Collects all query results in a list and returns it. Not that for queries returning very large result sets, this terminator will consume more memory and CPU time. |
|  `.toSet()`  | Collects all query results in a set (removing duplicates) and returns it. Not that for queries returning very large result sets, this terminator will consume more memory and CPU time. |


### Traversal Sources

Every query must begin with a traversal *source* which retrieves an initial iterator over nodes from the graph which can then be modified by further traversal steps. The most simple source is `V()` which includes all nodes of the graph.


| Traversal Source | Description |
|---|---|
|  `.V()`  | All nodes |
|  `.translationunits()`  | All `TranslationUnit` nodes, i.e. all source code files |
|  `.methods()`  | All `MethodDeclaration` nodes, i.e. all class methods |
|  `.functions()`  | All `FunctionDeclaration` nodes, i.e. all class methods and functions |
|  `.calls()`  | All `CallExpression` nodes, i.e. all method and function calls |
|  `.vars()`  | All `VariableDeclaration` nodes, i.e. all program locations declaring a variable |
|  `.fields()`  | All `FieldDeclaration` nodes |
|  `.ctor(String type)`  | All constructors of the given fully qualified type |


### Traversal Steps

Traversal sources and steps are the main operators of Crymlin queries. They determine which nodes and properties are retrieved from the graph. The Crymlin query language accepts all traversal steps supported by Apache Gremlin, plus the following steps which are specific to the exploration of the code property graph.

| Traversal Step | Description |
|---|---|
|  `.label()`  | Label of CPG node. The node label is the type of the CPG node (e.g., `MethodDeclaration`) |
|  `.name()`  | Name of the selected nodes as a string |
|  `.code()`  | Source code of the selected nodes as a string |
|  `.body()`  | Body of a function or method as a node. If the body contains more than one statement, this will typically return a `CompoundStatement` node  |