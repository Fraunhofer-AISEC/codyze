---
title: "Write MARK Policies"
linkTitle: "Write MARK Policies"
weight: 2
description: >
  MARK is a policy language to describe the correct and safe use of cryptographic libraries.
---

Codyze parses source code and checks it for compliance with predefined policies. These policies are written in a domain-specific language called _MARK_ and describe the correct and expected use of security-critical APIs. For example, a correct usage of cryptographic libraries will require certain key lengths, cryptographic primitives, and a certain order of API calls.

Codyze comes with a set of MARK policies for the _Botan_ (C++) and _Bouncycastle_ (Java) libraries, but MARK policies can be added anytime and are not necessarily limited to cryptographic libraries.

While MARK policies are simple text files and can be created with any text editor, we recommend [installing the Eclipse plugin](installation) which comes with syntax highlighting and code completion for editing MARK files.

MARK policies are separated into _Entities_ and _Rules_. Entities describe and group API functions at an abstract level and declare MARK variables that refer to function arguments or return values. Rules describe the expected usage of these entities. A violation of a rule will result in a _Finding_ and shown as a warning or error in the developer's IDE.