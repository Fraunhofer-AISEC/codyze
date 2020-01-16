---
title: "Overview"
linkTitle: "Overview"
weight: 1
description: >
  How everything works together.
---


Codyze is a static analysis tool to support developers in correctly using cryptographic libraries. 

Cryptography is hard and implementing it correctly is even harder. Luckily, there are well-established and battle-proved libraries available that do the heavy lifting of efficient and side-channel resilient implementation of cryptographic primitives. But even when using these libraries in application code, developers run the risk of making subtle errors that may undermine the security of their application. This is where {{< param product >}} helps. It integrates into the IDE or CI pipeline, analyses source code and tells developers if they are using cryptographic libraries in a correct or in an insecure way.


## How does it work?

In contrast to many other static analysis tools, Codyze directly analyzes the source code and does not require a compiler toolchain. It can thus even analyse incomplete source code and tolerate small syntax errors.

Codyze is based on a "Code Property Graph", which represents the source code as a graph and adds semantic information to support the analysis. This representation can be used in two ways:

1. as a fully automated tool, integrated into your CI or IDE
2. as a database that can be manually explored using a simple query language  


## Why Codyze?

Codyze checks source code for the correct usage of libraries. It is an addition to generic static analysis tools such as Sonarqube, Frama-C, or Checkerframework and specifically verifies that libraries are used as originally intended by their developers. 

**Library developers** write rules in a simple domain specific language, called *MARK*.

**Developers** using the library verify their code against these rules using Codyze - either as an IDE-plugin or as a CI module.

**Integrators** of open source components may want to verify these components using the automated analyis of Codyze or by manually search the code for critical patterns, using the *Crymlin* query language.

## Where should I go next?

* [MARK Authors](/mark-authors/): You are author of a cryptographic library? Start here to learn how to write MARK rules for your library.
* [Developers](/developers/): Learn how to use Codyze to check your application code
* [Contributors](/contributors): Start here to contribute to this documentation or to the project itself.

