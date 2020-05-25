---
title: "MARK Rules"
linkTitle: "MARK Rules"
no_list: true
weight: 3
date: 2020-02-05
description: >
  Built-ins of MARK
---

Once the core functions of a cryptographic library have been modeled as MARK entities, you can start writing rules. MARK rules operate over instances of entities and define conditions which must apply to these instances. A MARK "instance" may correspond to an actual object in the program, but in the case of non-object oriented languages or static methods, it may simply be an abstract set of function calls and variables.

### Basic rule structure

MARK rules are structured as follows:

<pre>
<span style="color:#204a87;font-weight:bold">rule</span> MyRule {
  <span style="color:#204a87;font-weight:bold">using</span>
    // instances go here
  <span style="color:#204a87;font-weight:bold">ensure</span>
    // conditions go here
  <span style="color:#204a87;font-weight:bold">onfail</span>
    // error message goes here
}
</pre>

Each rule has a name which must be unique across along all MARK files loaded into Codyze. The `using` keyword starts the declaration of instances of MARK entities and the `ensure` keyword starts the actual condition. If Codyze finds a violation of the condition in the program, it will issue a finding with the message indicated by the `onfail` identifier.

To illustrate the process of writing MARK rules, let us assume we want to ensure that the MARK entity `Crypto` from the previous section uses either of the two cryptographic algorithms `Algo1` or `Algo2` and that the algorithm is initiated with a parameter which is longer than 16 bytes.

<pre>
<span style="color:#204a87;font-weight:bold">rule</span> ID_2 {
  <span style="color:#204a87;font-weight:bold">using</span>
    Crypto <span style="color:#204a87;font-weight:bold">as</span> c,                   // instance c of MARK entity Crypto
    CryptoParameter <span style="color:#204a87;font-weight:bold">as</span> cp          // instance cp of MARK entity CryptoParameter
  <span style="color:#204a87;font-weight:bold">ensure</span>
    _is(c.param, cp)               // variable c.param == cp
    && _length(cp.rawData) >= 16   // byte length of cp.rawData >= 16
  <span style="color:#204a87;font-weight:bold">onfail</span>
  // todo
}
</pre>


### Preconditions

Some rules only apply if certain preconditions are fulfiled, i.e. such preconditions will be evaluated before the actual condition. If they fail, the main condition will not be evaluated and the rule will not return any result (i.e. it will neither confirm a valid program nor flag a wrong program). Preconditions follow the same syntax as the main condition, but are declared by the `when` keyword.


<pre>
<span style="color:#204a87;font-weight:bold">rule</span> ID_2 {
  <span style="color:#204a87;font-weight:bold">using</span>
    Crypto <span style="color:#204a87;font-weight:bold">as</span> c,                   // instance c of MARK entity Crypto
    CryptoParameter <span style="color:#204a87;font-weight:bold">as</span> cp          // instance cp of MARK entity CryptoParameter
  <span style="color:#204a87;font-weight:bold">when</span>
    c.algorithm == "Algo1"         // rule is only relevant for "Algo1"
  <span style="color:#204a87;font-weight:bold">ensure</span>
    _is(c.param, cp)               // variable c.param == cp
    && _length(cp.rawData) >= 16   // byte length of cp.rawData >= 16
  <span style="color:#204a87;font-weight:bold">onfail</span>
  // todo
}
</pre>

### Built-in Predicates

MARK comes with a number of built in functions that can be used as predicates in conditions and preconditions. These built-ins are called when MARK rules are evaluated and operate over their input arguments (typically MARK objects or constants) and the current evaluation context. By convention, built-ins should start with an underscore (`_`). When a built-in fails, it will return an `Error` object that always evaluates to _not applicable_, i.e. neither true nor false.

#### _has_value

__Function:__ `__has_value(a,b)`

__Example:__ 
```
  ensure
     __has_value(a, b)
```
Returns `true` if MARK object `a` may be assigned value `b` (e.g., a constant).

#### _inside_same_function

__Function:__ `__inside_same_function(a,b)`

__Example:__ 
```
  ensure
     __inside_same_function(a, b)
```
Returns `true` if MARK objects `a` and `b` reside in the same function.

#### _is

__Function:__ `_is(a,b)`

__Example:__ 
```
  ensure
     _is(a, b)
```
Returns `true` if MARK object `a` is equal to variable `b`.

#### _split

__Function:__ `_split(String str, String splitter, int position)`

__Example:__  TODO

Behaves like the Java expression `String.split(String splitter)[position]`. That is, it splits the string `str` at all occurrences of `splitter` and returns the `position`th substring. Returns an `Error` if no such occurrence is found.