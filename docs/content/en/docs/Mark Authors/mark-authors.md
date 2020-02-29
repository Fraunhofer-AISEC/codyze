---
title: "MARK Rules"
linkTitle: "MARK Rules"
weight: 3
date: 2020-02-05
description: >
  Built-ins of MARK
---

MARK comes with a number of built in functions that can be used as predicates in rules. These built-ins are called when MARK rules are evaluated and operate over their input arguments (typically MARK objects or constants) and the current evaluation context. By convention, built-ins should start with an underscore (`_`). When a built-in fails, it will return an `Error` object that always evaluates to _not applicable_, i.e. neither true nor false.

## _has_value

TODO

## _inside_same_function

__Function:__ `__inside_same_function(a,b)`

__Example:__ 
```
  ensure
     __inside_same_function(a, b)
```
Returns `true` if MARK objects `a` and `b` reside in the same function.

## _is

__Function:__ `_is(a,b)`

__Example:__ 
```
  ensure
     _is(a, b)
```
Returns `true` if MARK object `a` is equal to variable `b`.

## _split

__Function:__ `_split(String str, String splitter, int position)`

__Example:__  TODO

Behaves like the Java expression `String.split(String splitter)[position]`. That is, it splits the string `str` at all occurrences of `splitter` and returns the `position`th substring. Returns an `Error` if no such occurrence is found.