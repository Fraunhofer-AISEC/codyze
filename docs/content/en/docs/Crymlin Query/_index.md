---
title: "Crymlin Query Language"
linkTitle: "Crymlin Query Language"
weight: 3
description: >
  Crymlin is a query language to explore code property graphs
---

Crymlin is an extension of the graph traversal language [Apache Gremlin](https://tinkerpop.apache.org/gremlin.html) that comes with additional expressions for exploring code property graphs.

A graph traversal is an iterator over elements of the graph (nodes, edges, labels, properties) that always starts with a *traversal source*, then continues with any number of *traversal steps* and ends with a single *terminator step*. To understand these concepts, let's consider a simple example:


```python
>>> q.methods()
     .name()
     .toList()
[main, toString, myMethod, someOtherMethod]
```

In this example, `methods()` is a *traversal source* that starts iterating the graph at all nodes representing a method. `name()` is a *traversal step* that retrieves the `name` property of these nodes and `toList()` is a *terminator* that collects all names in a list. Every graph traversal needs a terminator to produce meaningful output. Until the console encounters a terminator, it will treat the graph traversal as an iterator that can be modified by further traversal steps.

### Query terminators

The most used terminators are:

| Terminator | Description |
|---|---|
|  `.next()`  | Returns the first element that matches the query or `NoSuchElementException` if the query result is empty. |
|  `.tryNext()`  | Returns the first element that matches the query or an empty `Optional` if the query result is empty. |
|  `.toList()`  | Collects all query results in a list and returns it. Not that for queries returning very large result sets, this terminator will consume more memory and CPU time. |
|  `.toSet()`  | Collects all query results in a set (removing duplicates) and returns it. Note that for queries returning very large result sets, this terminator will consume more memory and CPU time. |
|  `.count()`  | Returns the number of elements matches by the query. |


### Traversal Sources

Every query must begin with a traversal *source* which retrieves an initial iterator over nodes from the graph which can then be modified by further traversal steps. The most simple source is `V()` which includes all nodes of the graph.


| Traversal Source | Description |
|---|---|
|  `.byID(int id)`  | Node with the give id. The id of a node can be retrieved by the `.id()` step. |
|  `.calls()`  | Calls to methods or functions (`CallExpression`) |
|  `.calls(String calleeName)`  | Calls to methods or functions matching this name (`CallExpression`) |
|  `.ctor(String type)`  | Constructors of the given fully qualified type (`ConstructExpression`) |
|  `.declarations()`  | All declarations (of fields, variables, records, etc. (any of `FieldDeclaration`, `VariableDeclaration`, `RecordDeclaration`, etc.) |
|  `.fields()`  |  Declarations of fields (`FieldDeclaration`) |
|  `.field(String fieldName)`  |  Declarations of fields with the given name (`FieldDeclaration`) |
|  `.functions()`  | Class methods and functions (`FunctionDeclaration`) |
|  `.methods()`  | Class methods (`MethodDeclaration`) |
|  `.records()`  | Records are classes, enums, structs (`RecordDeclaration`) |
|  `.records(String name)`  | Records such as classes, enums, structs that match the given name (`RecordDeclaration`) |
|  `.translationunits()`  | File names of matching source code (`TranslationUnit`) |
|  `.vars()`  | Declarations of variables (`VariableDeclaration`) |
|  `.V()`  | All nodes in the graph |


### Traversal Steps

Traversal sources and steps are the main operators of Crymlin queries. They determine which nodes and properties are retrieved from the graph. The Crymlin query language accepts all traversal steps supported by Apache Gremlin, plus the following steps which are specific to the exploration of the code property graph.

| Traversal Step &nbsp; &nbsp;| Description |
|-----------------------|---|
|  <span style="white-space: nowrap;">`.argument(int n)`</span>  | Returns the the n-th argument of a function or method call (`ArgumentExpression`)  |
|  `.body()`  | Body of a function or method as a node. If the body contains more than one statement, this will typically return a `CompoundStatement` node  |
|  `.code()`  | Source code of the selected nodes as a string |
|  `.comment()`  | Any inline comment of the current CPG node |
|  `.file()`  | Name of the source code file containing the current CPG node |
|  `.label()`  | Label of CPG node. The node label is the type of the CPG node (e.g., `MethodDeclaration`) |
|  `.name()`  | Name of the selected nodes as a string |
|  `.nextCfg()`  | Returns the next node(s) along the control flow graph (CFG)  |
|  `.prevCfg()`  | Returns the next node(s) along the control flow graph (CFG)  |
|  `.nextEog()`  | Next node(s) along the Evaluation Order Graph (EOG) |
|  `.prevEog()`  | Previous node(s) along the Evaluation Order Graph (EOG) |
|  `.nextDfg()`  | Next node(s) along the Evaluation Order Graph (EOG) |
|  `.prevDfg()`  | Previous node(s) along the Evaluation Order Graph (EOG) |