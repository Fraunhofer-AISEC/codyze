
Codyze provides a built-in domain specific language, __Coko__, which can be used to define policies for Codyze.

## Structure of Policies

Coko policies can be split into two parts:

* Modelling the API through classes and functions.
* Writing rules that describe the expected usage of the modelled API.

When modeling a library, you will typically start by describing its classes or functions and then write rules.

## Structure of Coko

Coko is defined as a custom Kotlin scripting language.
It serves as an API to write source code queries in a declarative way.
The rules written in Coko are executed to construct the queries for the used backend.

The concept of Coko is that the API is modelled through classes and functions.
These classes and functions are used to declare rules, which Codyze then evaluates. 
Coko is, therefore, in its concept more similar to a domain specific language and only uses the Kotlin scripting technology to load the policies into Codyze.
However, as a Kotlin script can contain any valid Kotlin code, it is also possible to execute arbitrary Kotlin code with Coko.
It is currently not possible to prevent this, but all Coko scripts that are available on our website and GitHub repository are validated by us to prevent any misuse.
For more information about custom Kotlin scripting languages, please refer to the [:fontawesome-solid-arrow-up-right-from-square: Kotlin documentation](https://kotlinlang.org/docs/custom-script-deps-tutorial.html){target=_blank} and the respective [:fontawesome-solid-arrow-up-right-from-square: Kotlin KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md){target=_blank}.

Syntax highlighting and code completion are available for Coko in any IDE that provides these for Kotlin.

The syntax of Coko is the same as the syntax of Kotlin.
For writing Coko policies you will need to know how to create classes, interfaces and functions in Kotlin.
Please refer to the [:fontawesome-solid-arrow-up-right-from-square: Kotlin Documentation](https://kotlinlang.org/docs/basic-syntax.html){target=_blank} for an overview.

### Type-safe builders

Coko also uses the concept of [:fontawesome-solid-arrow-up-right-from-square: type-safe builders](https://kotlinlang.org/docs/type-safe-builders.html){target=_blank}.
Type-safe builders allow you to build objects in a semi-declarative way similar to markup languages.
They can be seen as a special syntax for nesting the construction of objects.
They will be explained in detail in the parts of Coko that use them.


