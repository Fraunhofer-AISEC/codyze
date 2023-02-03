
Codyze provides a built-in domain specific language, __Coko__, which can be used to define policies for Codyze.

## Structure of Policies

Coko policies can be split into two parts:

* Modelling the API through classes and functions.
* Writing rules that describe the expected usage of the modelled API.

When modeling a library, you will typically start by describing its classes or functions and then write rules.

## Structure of Coko

Coko is defined as a custom Kotlin scripting language.
It serves as an API to write queries for the backend of Codyze in a declarative way.
The functions written in Coko are therefore executed to construct the queries.

The concept of Coko is that the API is modelled through classes and functions.
These classes and functions are used to declare rules which Codyze evaluates. 
Coko is therefore in its concept more similar to a domain specific language and only uses the Kotlin scripting technology to load the policies into Codyze.
However, as a Kotlin scripting language, it is also possible to execute arbitrary Kotlin code with Coko.
It is currently not possible to prevent this, but all Coko scripts that are available on our website and GitHub repository are validated by us to prevent any misuse.
For more information about custom Kotlin scripting languages, please refer to the [Kotlin documentation](https://kotlinlang.org/docs/custom-script-deps-tutorial.html) and the [Kotlin KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md)

The syntax of Coko is the same as the syntax of Kotlin.
For writing Coko policies you will need to know how to create classes, interfaces and functions in Kotlin.
Please refer to the [Kotlin Documentation](https://kotlinlang.org/docs/getting-started.html) for an overview.

Syntax highlighting and code completion are available for Coko in any IDE that provides these for Kotlin.
