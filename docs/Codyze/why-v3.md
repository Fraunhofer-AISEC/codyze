---
title: "Why Codyze v3?"
linkTitle: "Why Codyze v3?"
date: 2022-07-12
no_list: true
description: >
  Differences between Codyze v3 and Codyze v2
---
The goal of the redesign is to make Codyze more maintainable and easier extendable.
This introduced a lot of changes compared to the legacy version.

The core functionalities of Codyze were separated from the executable part which makes it possible to use Codyze as a library.

We introduced the concept of Executors which are responsible for evaluating rules of a specific specification language.
Through Executors, Codyze can verify rules written in different specification languages and utilize their advantages as long as there is an Executor for them.

We're also working on a new specification language Coko that improves upon MARK.

Codyze has also been split into a frontend and a backend.
The frontend is responsible for parsing the rules and the analysis and the backend provides the information needed for the analysis.
Through this separation, another backend other than the CPG could be used for the analysis.

Additionally, we reworked the organisation of code in Codyze to be able to handle multiple projects with their own configurations with only one Codyze instance.
