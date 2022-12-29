---
title: "Documentation"
linkTitle: "Documentation"
weight: 20
no_list: true
menu:
  main:
    weight: 20
description: >
  Codyze is a static analysis tool to support developers in verifying compliance to security requirements.
---

!!! info

    Codyze is currently being redesigned.
    For the documentation for the legacy version of Codyze, please look [here](https://github.com/Fraunhofer-AISEC/codyze/tree/v2.3.0/docs).

Security is hard and implementing it correctly is even harder. Luckily, there are well-established and battle-proven libraries available that do the heavy lifting of security functions such as authentication, logging or encryption. But even when using these libraries in application code, developers run the risk of making subtle errors which may undermine the security of their application. This is where Codyze helps. By integrating it into an IDE or CI pipeline, developers can analyze their source code while programming and check if they are using libraries in a correct or in an insecure way.

## How does it work?

In contrast to many other static analysis tools, Codyze directly analyzes the source code and does not require a compiler tool-chain. It can thus even analyze incomplete source code and tolerate small syntax errors.

Codyze is based on a "Code Property Graph", which represents the source code as a graph and adds semantic information to support the analysis. This representation can be used in two ways:

1. as a fully automated tool, integrated into your CI or IDE
2. as a database that can be manually explored using a simple query language  

![Overview of Codyze](../assets/img/overall-view-white-background.png#only-light){ align=center }
![Overview of Codyze](../assets/img/overall-view-black-background.png#only-dark){ align=center }


## Why Codyze?

Codyze checks source code for the correct usage of libraries. It is an addition to generic static analysis tools such as Sonarqube, Frama-C, or the Checker Framework and specifically verifies that libraries are used as originally intended by their developers. 

**Library developers** write rules for their library in specification languages supported by Codyze.

**Developers** verify their code against rules of modelled libraries using Codyze.

**Integrators** of open source components may want to verify these components using the automated analysis of Codyze or by manually searching the code for critical patterns.



