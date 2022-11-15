---
title: "Write MARK Policies"
linkTitle: "Write MARK Policies"
weight: 4
description: >
  MARK is a policy language to describe the correct usage patterns of API or libraries.
---

Codyze parses source code and checks it for compliance with predefined policies. These policies are written in a domain specific language called _MARK_ and describe the correct and expected use of, for example security-critical, APIs.

MARK policies are separated into _Entities_ and _Rules_.

* [Entities](Define%20Entities.md) describe and group API functions at an abstract level and declare MARK variables that refer to function arguments or return values.
* [Rules](mark-authors.md) describe the expected usage of these entities. A violation of a rule will result in a _Finding_ and is shown as a warning or error in the developer's IDE.

When modeling a library, you will typically start by describing its classes or functions as MARK entitites and then write rules.

Codyze comes with a set of MARK policies for the _Botan_ (C++), _Bouncycastle_ (Java) and _Jackson_ (Java) libraries, but MARK policies for other libraries can be added anytime.

MARK policies are simple text files and can be created with any text editor, but we recommend [installing the Eclipse plugin](installation.md) which comes with syntax highlighting and code completion for MARK files.
