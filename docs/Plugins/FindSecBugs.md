---
title: "FindSecBugs Plugin"
linkTitle: "FindSecBugs Plugin"
no_list: true
date: 2023-01-24
description: >
  The FindSecBugs Plugin aims to report security bugs in compiled Java code.
---

!!! info

    Check out the official site [here](https://find-sec-bugs.github.io/).

## Plugin overview

FindSecBugs is an extension of the SpotBugs analyzer that works on compiled Java code.
It focuses on finding security-critical bugs such as potential code injections.

!!! bug

    Using the FindSecBugs plugin may mark the analysis run as unsuccessful when using lambdas.
    This is a [known issue](https://github.com/find-sec-bugs/find-sec-bugs/issues/692) within SpotBugs

!!! question "How does FindSecBugs use the context?"

    FindSecBugs relies on the compiled code of the libraries to resolve all code references.
    Therefore, the context should point to those libraries in order to ensure a complete analysis.

    


