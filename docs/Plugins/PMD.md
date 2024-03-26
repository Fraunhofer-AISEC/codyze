---
title: "PMD Plugin"
linkTitle: "PMD Plugin"
no_list: true
date: 2023-01-24
description: >
  The PMD Plugin aims to report security bugs in compiled Java code.
---

!!! info

    Check out the official site [here](https://pmd.github.io/).

## Plugin overview

PMD is a source code analyzer that searches for common programming flaws.
It supports many different languages and can be extended by different sets of rules.

In its current implementation the plugin uses the following sets of rules:
 - all-java.xml ([link](https://github.com/pmd/pmd/blob/83522e96ef512f2b9a41586ae239509ec6f8313f/pmd-core/src/main/resources/rulesets/internal/all-java.xml))

!!! note

    These rules define the supported languages as well as the flaws found in those languages.
    They may be extended in future updates.

!!! question "How does PMD use the context?"

    PMD does not rely on addition context, this option is therefore ignored.