
Rules in Coko describe how an API should be used.
They are functions that are annotated with the [`@Rule` <i class="fas fa-external-link-alt"></i>](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl/-rule){target=_blank} annotation.

In the `@Rule` annotation you can specify metadata about the rule such as the description of the rule.
The metadata will be used for describing the findings in the SARIF output.

If the rule requires some instance of a model, they can be specified as parameters to the rule function. 

Each Coko rule must return an implementation of the [`Evaluator` <i class="fas fa-external-link-alt"></i>](../../api/codyze/codyze-specification-languages/coko/coko-core/de.fraunhofer.aisec.codyze.specificationLanguages.coko.core/-evaluator){target=_blank} interface, which Codyze can use to evaluate the rule.
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
    
    fun second(s: Any?) = op {
        definition("Foo.second") {
            signature(s)
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
    only(foo.first(1))

```


## Order Evaluator
The `order` evaluator checks if functions related to an object are called in the correct order.
It takes two arguments, the `baseNodes` and the order.
The `baseNodes` are the function calls that are the start of the order.
Usually, this is either the constructor of a class or some kind of initialization function.

To construct the order, Coko provides a [type-safe builder](index.md#type-safe-builders).
Within the builder, the order is specified as a regular expression.

The "alphabet" of the order regex is:

- references to [functions that return an `Op`](modelling.md#functions) written as `<object>::<FunctionName>` or `::<FunctionName>`
- `Ops` themselves.

If all calls to a modelled function should be considered for the order regardless of the specified signatures, please use the first option.
When passing `Ops`, only functions that match the used signature and argument are considered valid.

The builder provides a set of functions that allow you to add quantifiers to the regex or group them.

| Function            | Regex          | Description                                                                      |
|---------------------|----------------|----------------------------------------------------------------------------------|
| `or`                | &#124;         | Represents a choice, either the first or the second expression has to be matched |
| `set`               | []             | Represents multiple choices, one expression in it has to be matched              |
| `maybe`             | *              | Matches an expression zero or more times                                         |
| `some`              | +              | Matches an expression one or more times                                          |
| `option`            | ?              | Matches an expression zero or one time                                           |
| `count(n)`          | {`n`}          | Matches an expression exactly `n` times                                          |
| `atLeast(min)`      | {`min`,}       | Matches an expression at least `min` times                                       |
| `between(min, max)` | {`min`, `max`} | Matches an expression at least `min` and at most `max` times                     |


```kotlin title="Rule example using order"
@Rule
fun `order of Foo`(foo: Foo) = 
    order(foo.constructor()/* (4)! */) { // (3)!
        - foo.first(...) // (2)!
        maybe(foo::second) // (1)!
    }

```

1. This will consider all calls to the function modelled by `foo.second()` for the order. No filter will be applied. 
2. This will use the filtered `Op` returned by `foo.first(...)` for the order.
3. This starts the type-safe builder for the order.
4. The `Op` returned from `foo.constructor` will be used as query for the function calls that are the starting point for evaluating the order.


## FollowedBy Evaluator
The `followedBy` evaluator works similarly like the implication in logic.
It takes two `Ops` and specifies that if the first `Op` is called then the second `Op` must be called as well.
Compared to the `order` evaluator, `followedBy` is more flexible because Ops from different models can be connected.

```kotlin title="Rule example using followedBy"
@Rule
fun `if first then second`(foo: Foo, bar: Bar) = 
    foo.first(Wildcard) followedBy bar.second()

```
