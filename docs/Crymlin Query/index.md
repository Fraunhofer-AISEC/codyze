---
title: "Crymlin Query Language"
linkTitle: "Crymlin Query Language"
weight: 3
expiryDate: 2022-05-24
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

So, the general structure of a Crymlin query is
```
 q.<traversal source>.<traversal steps>. ... .<terminator>
```

### Traversal Sources

Every query must begin with a traversal *source* which retrieves an initial iterator over nodes from the graph which can then be modified by further traversal steps. The most simple source is `V()` which includes all nodes of the graph.


| Traversal Source | Description |
|---|---|
| `.callsFqn(String fqn)`           | Calls to functions/methods (`CallExpression`) whose (fully qualified) name _matches exactly_ the argument. |
| `.byID(long)`                     |	Node by its ID (`Node`) |
| `.calls()`                        |	All function/method calls (`CallExpression`) |
| `.calls(String)`                  |	Calls to functions/methods whose (fully qualified) name contains the argument. (`CallExpression`) |
| `.ctors(String)`                  |	Constructors containing a given type (`ConstructExpression`) |
| `.fields()`                       |	Field declarations (`FieldDeclaration`) |
| `.functions()`                    |	All functions/methods (`FunctionDeclaration`, `MethodDeclaration`) |
| `.functions(String)`              |	Functions/methods containing the given name (`FunctionDeclaration`, `MethodDeclaration`) |
| `.ifstmts()`                      |	All IfStatements (`IfStatement`) |
| `.ifstmts(String)`                |	IfStatements whose code contains the given substring (`IfStatement`) |
| `.methods()`                      |	All class methods (Note: rather use 'functions()' to include C/C++ functions) (`MethodDeclaration`) |
| `.methods(String)`                |	Class methods containing the given name (Note: rather use 'functions()' to include C/C++ functions)  (`MethodDeclaration`) |
| `.namespaces()`                   |	Namespaces (`NamespaceDeclaration`) |
| `.namespaces(String)`             |	Namespaces containing the given substring (`NamespaceDeclaration`) |
| `.nextCallByID(long)`             |	Next call statement following node by ID (`CallExpression`) |
| `.prevCallByID(long)`             |	Next call statement following node by ID (`CallExpression`) |
| `.records()`                      |	All RecordDeclarations (Java classes, enums, C/C++ structs) (`RecordDeclaration`) |
| `.records(String)`                |	RecordDeclarations (Java classes, enums, C/C++ structs) containing the given name (`RecordDeclaration`) |
| `.returns()`                      |	All return statements (`ReturnStatement`) |
| `.sourcefiles()`                  |	All source code files (`TranslationUnit`) |
| `.sourcefiles(String)`            |	Source code file containing the given name (`TranslationUnit`) |
| `.statements()`                   |	Statements (`Statement`) |
| `.typedefs()`                     |	All typedefs (`TypedefDeclaration`) |
| `.typedefs(String)`               |	All typedefs containing the given name  (`TypedefDeclaration`) |
| `.valdecl()`                      |	All declarations of values (parameters, variables, fields, enums constants) (`ValueDeclaration`) |
| `.valdecl(String)`                |	All declarations of values (parameters, variables, fields, enums constants) containing the given name (`ValueDeclaration`) |
| `.vars()`                         |	Variable declarations. Use valdecl() instead to include parameters, fields, and enums. (`VariableDeclaration`) |
|  `.V()`                           |   All nodes in the graph |




### Traversal Steps

Traversal sources and steps are the main operators of Crymlin queries. They determine which nodes and properties are retrieved from the graph. The Crymlin query language accepts all traversal steps supported by Apache Gremlin, plus the following steps which are specific to the exploration of the code property graph. Please refer to the [Apache Gremlin Specification](https://tinkerpop.apache.org/gremlin.html) for further supported traversal steps.

| Traversal Step &nbsp; &nbsp;| Description |
|-----------------------|---|
|  <span style="white-space: nowrap;">`.argument(int n)`</span>  | Returns the the n-th argument of a function or method call (`ArgumentExpression`)  |
| `code()`                            |	Original source code of the selected node(s) |
| `comment()`                         |	Comments attached to the selected node(s) |
| `condition()`                       |	Condition (of an IfStatement) |
| `condition(String`)                 |	Condition (of an IfStatement) containing the given substring |
| `elseStmt()`                        |	'else' block (of an IfStatement) |
| `line()`                            |	Source code line of the selected node(s) |
| `name()`                            |	Name of the selected node(s) |
| `nextCfg()`                         |	Next node(s) in Control Flow Graph |
| `nextEog()`                         |	Next node(s) in Evaluation Order Graph |
| `prevCfg()`                         |	Previous node(s) in Control Flow Graph |
| `prevEog()`                         |	Previous node(s) in Evaluation Order Graph |
| `thenStmt()`                        |	'then' block (of an IfStatement) |
| `type()`                            |	Type of the node(s) |

### Query terminators

The most used terminators are:

| Terminator | Description |
|---|---|
|  `.next()`  | Returns the first element that matches the query or `NoSuchElementException` if the query result is empty. |
|  `.tryNext()`  | Returns the first element that matches the query or an empty `Optional` if the query result is empty. |
|  `.toList()`  | Collects all query results in a list and returns it. Not that for queries returning very large result sets, this terminator will consume more memory and CPU time. |
|  `.toSet()`  | Collects all query results in a set (removing duplicates) and returns it. Note that for queries returning very large result sets, this terminator will consume more memory and CPU time. |
|  `.count()`  | Returns the number of elements matches by the query. |

