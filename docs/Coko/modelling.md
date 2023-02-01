
To evaluate rules for an API, Coko needs an understanding of the components of the API, such as classes or functions.
In Coko these components are modelled through interfaces, classes and a class called `Op`.

## Interfaces

A goal of Coko is to make its rules more reusable.
Aside from specific rules of an API, there might be some general policies that should be followed for all API that provide similar functionality.
An example would be that all actions executed on a database should be logged.
Instead of writing the corresponding rule for all combinations of database and logging APIs, one might want to combine this into one reusable rule.

This reusability is achieved through interfaces.
In Coko interfaces and their functions describe the functionalities that a group of APIs has in common.
Rules can therefore be written on a more conceptual level.

## Classes

Classes in Coko model the actual components of an API.
They can implement interfaces or just be normal classes.
With classes, API functions can be grouped

For APIs written in object-oriented languages it might be sensible to create a Coko class for each class in the API.


## Methods and Ops

Methods of classes in Coko are used to group functions that serve the same functionality in an API.
This grouping is achieved through [`Ops`](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl/-op).


