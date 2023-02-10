
Rules in Coko describe how an API should be used.
They are functions that are annotated with the [`@Rule`](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl/-rule) annotation.

In the `@Rule` annotation you can specify metadata about the rule such as the description of the rule.
The metadata will be used for describing the findings in the SARIF output.

If the rule requires some instance of a model, they can be specified as parameters to the rule function. 

Each Coko rule must return an implementation of the [`Evaluator`](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core/-evaluator) interface which Codyze can use to evaluate the rule.
Coko provides some common evaluators which will be explained in the following sections.
The example model will be used for explaining the evaluators.  

```kotlin title="Example model"
class Foo {
    fun constructor() = constructor("Foo") {
        signature()
    }
    
    fun first(i: Any?) = op {
        definition("Foo.first") {
            signature(i)
        }
    }
    
    fun second() = op {
        definition("Foo.second") {
            signature()
        }
    }
}

class Bar {
    fun second() = op {
        definition("Bar.second") {
            signature()
        }
    }
}
```

## Only Evaluator
The `only` evaluator checks if all calls to an Op are only called with the specified arguments.
Therefore, it takes one `Op` as argument.

```kotlin title="Rule example using only"
@Rule
fun `only calls to first with 1 allowed`(foo: Foo) = 
    only(foo.f(1))

```


## Order Evaluator
The `order` evaluator checks if functions related to an object are called in the correct order.
It takes two arguments, the `baseNodes` and the order.
The `baseNodes` are the function calls which are the start of the order.
Normally, this is either the constructor of a class or some kind of initialization function.

To construct the order, Coko provides a [type-safe builder](index.md#type-safe-builders).
The builder provides a set of functions that allow you to specify the order as a regex.
The "alphabet" of the order are functions that return an `Op`.


```kotlin title="Rule example using order"
@Rule
fun `order of Foo`(foo: Foo) = 
    order(foo.constructor()) {
        +Foo::first
        maybe(Foo::second)
    }

```


## FollowedBy Evaluator
The `followedBy` evaluator works similarly like the implication in logic.
It takes two Ops and specifies that if the first Op is called then the second Op must be called as well.
Compared to the `order` evaluator, `followedBy` is more flexible because Ops from different models can be connected.

```kotlin title="Rule example using followedBy"
@Rule
fun `if first then second`(foo: Foo, bar: Bar) = 
    foo.first(Wildcard) followedBy bar.second()

```
