
To evaluate rules for an API, Coko needs an understanding of the components of the API, such as classes or functions.
In Coko these components are modelled through interfaces, classes and a class called `Op`.

## Ops

[`Ops` <i class="fas fa-external-link-alt"></i>](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl/-op){target=_blank} are the basic building blocks for writing policies in Coko.
With `Ops` you can model and group functions of the API that serve a similar functionality.
They are also a way to define queries to the Codyze backend for finding calls to these functions.
Each `Op` object is one query to the backend.

There are currently two types of `Ops`, `FunctionOps` for modelling functions and `ConstructorOps` for modelling constructors in object-oriented languages.
They are both built with [type-safe builders](index.md#type-safe-builders).
The following sections will explain the builders for each `Op` type.

### FunctionOps

The function [`op()` <i class="fas fa-external-link-alt"></i>](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl/op.html){target=_blank} is the start for building `FunctionOps`.
Within the block of `op()` the fully qualified name of the functions you want to model can be specified as a string.
In the block of the fully qualified name the arguments to function can be defined.
They serve as a filter for the query.
Only calls to the function with the same number of arguments with the same value at each position will be found by the query.

In `signature` it is also possible to specify unordered arguments.
These are arguments, that should somehow be passed to the function calls we want to find, but it is not important, in which position they are passed.

```kotlin title="Example of defining a FunctionOp"
op {
    "my.fully.qualified.name" { // (1)!
        signature(5..10) // (2)!
        signature(".*one.*") // (3)!
        signature(0..5, listOf("one", "two")) // (4)!
        signature() // (9)!
    }
    
    "my.other.fully.qualified.name" { // (5)!
        signature { // (6)!
            - ".*" // (7)!
            -7 // (8)!
        }
        signature(arrayOf(4)) { // (10)!
            - 123
        }
    }
}
```

1. The fully qualified name of the function we want to find.
2. Filters for calls to `my.fully.qualified.name` that have as only argument a number between 5 and 10.
3. Filters for calls to `my.fully.qualified.name` that have a string as only argument that contains "one", for example `my.fully.qualified.name("tone")`.
4. Filters for calls to `my.fully.qualified.name` that have a number between 0 and 5 as first argument and as second argument either the string "one" or "two".
5. The fully qualified name of the other function we want to find.
6. The `signature` function can also invoke a type-safe builder.
7. In the type-safe builder of `signature` the arguments are listed using `-`.
8. The space after the `-` is optional.
9. Filters for calls to `my.fully.qualified.name` where no arguments were passed.
10. The unordered arguments are given as an array to `signature`. In this example, the unordered argument is 4.

### ConstructorOps

The function of the builder for `ConstructorOps` is [`constructor()` <i class="fas fa-external-link-alt"></i>](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl/constructor.html){target=_blank}.
The fully qualified name of the class is the first argument.
In the block of `constructor()` you can specify the arguments to the constructor like for the `FunctionOp`.
They serve the same purpose as for [`FunctionOps`](modelling.md#functionops).

```kotlin title="Example of defining a ConstructorOp"
constructor("my.fully.qualified.MyClass") { // (1)!
    signature() // (2)!
}
```

1. This models the constructor of the class `my.fully.qualified.MyClass`.
2. The signature is directly specified in `ConstructorOps`, because the name of the constructor is clear. 

### Special argument types

Arguments in `Ops` are used to filter calls of a function based on the arguments that are given to the calls.
In Coko there are a few special argument types that help with the filtering.

The first type is the `Wildcard` object.
If the `Wildcard` object is given as an argument to `Op`, the filter will allow any kind of value in the same argument position of the function calls.

The second type is `null`.
It can be used to signify that the given arguments should be filtered out of the query.
This type will be helpful when constructing [`Op` templates with Coko functions](modelling.md#functions).

Another special type are `ParameterGroups`.
They are a filter to express that the argument at the position should be composed of multiple values.
An example would be if the argument is a string that should contain multiple strings, for example `foo("Start string" + 12 + "End string")`.
This can be modeled with `ParameterGroups`.
Coko offers the DSL function `group`, in which these values can be specified. 

The last types are the `Type` class and the `ParamWithType` class.
The `Type` class is used to filter the type of the arguments.
The fully qualified name of the type must be given
`ParamWithType` combines the filter for the value with the filter for the type.


```kotlin title="Example with special argument types"
op {
    "my.fully.qualified.name" {
        signature(Wildcard) // (1)!
        signature(null, 1) // (2)!
        signature(Wildcard, 2) // (3)!
        signature( 
            group {
                - "Start string .*"
                - 12
            } // (6)!
        )
        signature(Type("java.util.List")) // (4)!
        signature(1.0 withType "java.lang.Float") // (5)!
    }
}
```

1. Queries for all calls to `my.fully.qualified.name` that have one argument.
2. Filters out all calls to `my.fully.qualified.name` with two arguments and where the second argument is 1.
3. Queries for all calls to `my.fully.qualified.name` with two arguments and where the second argument is 2.
4. Queries for all calls to `my.fully.qualified.name` with one argument which must be of type `java.util.List`[^1].
5. Queries for all class to `my.fully.qualified.name` with one argument which has the value 1.0 and has the type `java.lang.Float`[^1].
6. Filters for all calls to `my.fully.qualified.name` with one argument. The argument must contain both `"Start string"` and the number 12. An example would be `my.fully.qualified.name("Start string with the number " + 12)`.

[^1]: In a real example this would be redundant because this case is already covered by the first filter with `Wildcard`.

## Functions

Since each `Op` is interpreted by Codyze as one query for function calls to the backend, it would be helpful to have templates for `Ops` that find calls to the same function but with different filters.
This can be achieved with functions in Coko.
The parameters of these functions in Coko can be used to pass the specific values to the filter.

```kotlin title="An example for an Op template for the function java.util.List.add"
fun add(
    element: Any, // (1)!
    index: Any? // (2)!
) = op {
    "java.util.List.add" {
        signature(element)
        signature(index withType "int", element) // (3)!
    }
}
```

1. `List.add` in Java is a generic method, so the type of `element` is not static. However, it is recommended to use `Any` even if the type of the value is static since you might want to pass one of the special argument types like `Wildcard`.
2. `index` is nullable, so it is also possible to filter for calls where no index is given. The type is `Any?` to be able to pass `Wildcard` as well. 
3. The arguments can be further specified like the additional filter for the type of `index`.  

If the reference to a Coko function is used for [`order` rules](rules.md#order-evaluator), all parameters must have a nullable type.
Coko invokes them with dummy arguments and uses internal functions to query for all calls to the modelled function regardless of the specified signatures. 


## Interfaces

A goal of Coko is to make rules more reusable.
Aside from specific rules of an API, there might be some general policies that should be followed for all API that provide similar functionality.
An example would be that all actions executed on a database should be logged.
Instead of writing the corresponding rule for all combinations of database and logging APIs, one might want to combine this into one reusable rule.

This reusability is achieved through interfaces.
In Coko interfaces and their functions describe the functionalities that a group of APIs has in common.
Rules can thus be written on a more conceptual level.

When Coko encounters rules using a Coko interface as a parameter, it will use one of the available classes implementing the interface as argument to evaluate the rule.
Currently, it uses the first class it finds, however, we will implement an algorithm in the future which will try to find the implementation that fits the best for the analyzed code.

```kotlin title="Example of a interface and its implementations in Coko"
interface Logging {
    fun log(message: Any?, vararg args: Any?): Op
}

class JavaLogging: Logging {
    override fun log(message: Any?, vararg args: Any?): Op =
        op {
            "java.util.logging.Logger.info" {
                signature {
                    group {
                        - message
                        args.forEach { - it }
                    }
                }
            } 
        }
}

class PythonLogging: Logging {
    override fun log(message: Any?, vararg args: Any?): Op =
        op {
            "logging.info" { 
                signature(args) { // (1)! 
                    - message 
                } 
            }
        }
}
```

1. We don't care about the order of the arguments for `args`, just that they appear somewhere as arguments for the call.

## Classes

Classes in Coko model the actual components of an API.
They can implement interfaces or just be normal classes.
With classes, API functions can be grouped

For APIs written in object-oriented languages it might be sensible to create a Coko class for each class in the API.
