---
title: "Why Codyze v3?"
linkTitle: "Why Codyze v3?"
date: 2022-07-12
no_list: true
description: >
  Differences between Codyze v3 and Codyze v2
---
The goal of Codyze v3 is to make Codyze more maintainable and easier extendable.
This introduced a lot of changes to Codyze v2.

The core functionalities of Codyze were separated from the executable part which makes it possible to use Codyze as a library as well.

We further introduced the concept of Executors which are responsible for evaluating rules of a specific specification language.
Through Executors, Codyze is no longer limited to MARK rules as long as there is an Executor for the specification language of your choosing.

With Codyze v3 we also offer a new specification language called MARK 2 (?).
// TODO Description

Additionally, we reworked the LSP mode of Codyze to be able to handle multiple projects with their own configurations with only one Codyze instance. // stimmt das?
This for example allows switching between projects in an IDE without losing the context of any analysis.

