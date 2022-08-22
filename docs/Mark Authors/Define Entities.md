---
title: "MARK Entities"
linkTitle: "MARK Entities"
date: 2019-12-10
no_list: true
weight: 2
description: >
  Describing the core objects of a library by means of MARK entities
---

MARK rules refer to _entities_ - abstract objects that wrap the real object classes of the analyzed programming language (in case of object-oriented languages) and group methods with similar semantics in so-called _op_&#8203;s. A MARK entity defines three parts:

1. A _name_
2. A set of _op_&#8203;s (operations)
3. A set of MARK _variables_

Writing MARK rules for a library requires a good understanding of the library API and its class hierarchy. We recommend the following approach to writing MARK entities.

1. Model relevant classes as MARK entities
2. Define _op_&#8203;s and variables
3. (Optionally) blacklist some _op_&#8203;s

## Model relevant classes as MARK entities

It is certainly not necessary to model all classes of the software library as MARK entities. Rather, you need to identify those classes which hold relevant data or provide functions. Although in many cases, several classes of the programming language can be summarized in one abstract MARK entity, it might be easier to directly map classes to entities in the first iteration.

The name of an entity can be freely chosen. If it refers to a specific class in the programming language, though, it might make sense to name them accordingly.

## Define Ops and variables

The next step is to define _op_&#8203;s. An _op_ is a group of semantically equal or similar functions, methods, or constructors, given as fully qualified signatures. Especially overloaded functions with the same name but different parameters are candidates for being grouped in an _op_. For cryptographic libraries, typical _op_&#8203;s are:

- `instantiate` - a group of functions for instantiating a class or creating an object of a class
- `initialize` - a group of functions that initialize a cryptographic algorithms, e.g. by setting a key or initialization vector
- `update` - a group of functions that process further data by a cryptographic algorithm
- `finalize`- a group of functions that terminate a cryptographic algorithm
- `reset`- a group of functions that reset a cryptographic algorithm and make it ready for further processing. 

The name of an _op_ can be freely chosen. When specifying fully qualified function or method signatures in an _op_, parameters are typed and can be _unnamed_ or _named_. Unnamed parameters are indicated by the name "_" and do not play any role in the definition of rules. Named parameters refer to MARK variables and can be used when writing rules. We recommend to name only those parameters which are required in rules, as named parameters will increase the memory cost and runtime of the analysis.

__Example:__ The following `op` _instantiate_ refers to only a single Java method, called `de.example.Crypto.getInstance`. Neither the return type, nor modifiers such as `public`, `static`, `final` etc. are given in MARK. The method signature contains one named parameter of type `java.lang.String` and one unnamed parameter without type restriction. Note that the name of the parameter does not relate to the parameter name in the programming language, but rather to a MARK variable.

<pre>
<span style="color:#204a87;font-weight:bold">op</span> instantiate {
  de.example.Crypto.getInstance(
    algorithm : java.lang.String,    // Named typed parameter
    _                                // Unnamed untyped parameter
  );
}
</pre>

So, this MARK `op` would include the following methods of a class `de.example.Crypto`:

* `public static Crypto getInstance(String x, String y)`
* `private void getInstance(String x, byte y)`

It would however _not_ include a method `void getInstance(String x)` (wrong number of parameters) or `getInstance(byte x, String y)` (wrong type of 1st parameter).

To make use of named parameters, they must additionally be declared as entity variables using the `var` keyword:

<pre>
<span style="color:#204a87;font-weight:bold">entity</span> Crypto {

  <span style="color:#204a87;font-weight:bold">var</span> algorithm;  // this makes parameter "algorithm" available when writing rules.

  <span style="color:#204a87;font-weight:bold">op</span> instantiate {
    de.example.Crypto.getInstance(
      algorithm : java.lang.String,    // Named typed parameter
      _                                // Unnamed untyped parameter
    );
  }
}
</pre>


## (Optionally) blacklist some Ops

In some cases, groups of functions or methods should not be used at all by a program. This applies e.g. to deprecated functions or functions that are known to be insecure. MARK provides a shortcut to mark any use of such functions as insecure: the `forbidden` keyword.

<pre>
<span style="color:#204a87;font-weight:bold">entity</span> Crypto {

  <span style="color:#204a87;font-weight:bold">op</span> instantiate {
    de.example.Crypto.getInstance(
      algorithm : java.lang.String,    // Named typed parameter
      _                                // Unnamed untyped parameter
    );
    <span style="color:#204a87;font-weight:bold">forbidden</span> de.example.Crypto.getInstanceDeprecated();  // Any use of this function will be flagged
  }
}
</pre>

Any occurrence of `getInstanceDeprecated()` in the program will be marked as insecure, without further evaluation of rules. This is not only a shortcut, removing the need to write separate rules, but also a way to cut down analysis time, as Codyze does not need to find instances of the entity, but will rather indicate the error immediately when it sees a usage of the method.


## Complete Example


<pre>
<span style="color:#204a87;font-weight:bold">entity</span> <span style="color:#c4a000">org.bouncycastle.crypto.digests.SHA512Digest</span> 
           <span style="color:#c4a000">isa org.bouncycastle.crypto.Digest</span> <span style="color:#ce5c00;font-weight:bold">{</span>
  
  <span style="color:#204a87;font-weight:bold">var</span> instance : org.bouncycastle.crypto.digests.SHA512Digest; // Alternative for `this`
  
  <span style="color:#204a87;font-weight:bold">var</span> encodedState : byte[];
  <span style="color:#204a87;font-weight:bold">var</span> copy : org.bouncycastle.crypto.digests.SHA512Digest;
  
  <span style="color:#204a87;font-weight:bold">var</span> inByte : byte;
  <span style="color:#204a87;font-weight:bold">var</span> inByteArray : byte[];
  <span style="color:#204a87;font-weight:bold">var</span> inByteArrayOff : int;
  <span style="color:#204a87;font-weight:bold">var</span> inByteArraylen : int;
  
  <span style="color:#204a87;font-weight:bold">var</span> outArray : byte[];
  <span style="color:#204a87;font-weight:bold">var</span> outArrayOff : int;
  <span style="color:#204a87;font-weight:bold">var</span> resultLen : int;
  
  
  <span style="color:#204a87;font-weight:bold">op</span> instantiate <span style="color:#ce5c00;font-weight:bold">{</span>
        this = org.bouncycastle.crypto.digests.SHA512Digest();
        this = org.bouncycastle.crypto.digests.SHA512Digest(encodedState);
        instance = org.bouncycastle.crypto.digests.SHA512Digest(copy);
    <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#204a87;font-weight:bold">op</span> update <span style="color:#ce5c00;font-weight:bold">{</span>
    org.bouncycastle.crypto.digests.SHA512Digest.update(inByte);
    org.bouncycastle.crypto.digests.SHA512Digest.update(inByteArray, inByteArrayOff, inByteArrayLen);
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#204a87;font-weight:bold">op</span> finish <span style="color:#ce5c00;font-weight:bold">{</span>
    org.bouncycastle.crypto.digests.SHA512Digest.finish();
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#204a87;font-weight:bold">op</span> finalize <span style="color:#ce5c00;font-weight:bold">{</span>
    resultLen = org.bouncycastle.crypto.digests.SHA512Digest.doFinal(outArray, outArrayOff);
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#204a87;font-weight:bold">op</span> reset <span style="color:#ce5c00;font-weight:bold">{</span>
    org.bouncycastle.crypto.digests.SHA512Digest.reset();
    org.bouncycastle.crypto.digests.SHA512Digest.reset(_);
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
<span style="color:#ce5c00;font-weight:bold">}</span>
</pre>