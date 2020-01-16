---
title: "MARK Entities"
linkTitle: "MARK Entities"
date: 2019-12-10
weight: 2
description: >
  Describing the core objects of a library by means of MARK entities
---

MARK rules refer to _entities_ - abstract objects that wrap the real object classes of the analyzed programming language. A MARK entity defines three parts:

1. A _name_
1. A set of _ops_ (operations)
1. A set of MARK _variables_

## Choosing a name

The name must reflect the fully qualified name of the class in the programing language.

## Defining Ops

An `op` wraps one or more methods with same or similar behavior. MARK rules refer to `op`s instead of methods to abstract away overloaded methods and simplify rules.

## Defining variables

Variables must be declared with the `var` keyword and _may_ have a type. Rules use variables to refer to either method arguments or return values.

## Example


<pre>
<span style="color:#204a87;font-weight:bold">entity</span> <span style="color:#c4a000">org.bouncycastle.crypto.digests.SHA512Digest</span> 
           isa org.bouncycastle.crypto.Digest <span style="color:#ce5c00;font-weight:bold">{</span>
  
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
  
  
  <span style="color:#ce5c00;font-weight:bold">op</span> instantiate <span style="color:#ce5c00;font-weight:bold">{</span>
        this = org.bouncycastle.crypto.digests.SHA512Digest();
        this = org.bouncycastle.crypto.digests.SHA512Digest(encodedState);
        instance = org.bouncycastle.crypto.digests.SHA512Digest(copy);
    <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#ce5c00;font-weight:bold">op</span> update <span style="color:#ce5c00;font-weight:bold">{</span>
    org.bouncycastle.crypto.digests.SHA512Digest.update(inByte);
    org.bouncycastle.crypto.digests.SHA512Digest.update(inByteArray, inByteArrayOff, inByteArrayLen);
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#ce5c00;font-weight:bold">op</span> finish <span style="color:#ce5c00;font-weight:bold">{</span>
    org.bouncycastle.crypto.digests.SHA512Digest.finish();
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#ce5c00;font-weight:bold">op</span> finalize <span style="color:#ce5c00;font-weight:bold">{</span>
    resultLen = org.bouncycastle.crypto.digests.SHA512Digest.doFinal(outArray, outArrayOff);
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
  <span style="color:#ce5c00;font-weight:bold">op</span> reset <span style="color:#ce5c00;font-weight:bold">{</span>
    org.bouncycastle.crypto.digests.SHA512Digest.reset();
    org.bouncycastle.crypto.digests.SHA512Digest.reset(_);
  <span style="color:#ce5c00;font-weight:bold">}</span>
  
<span style="color:#ce5c00;font-weight:bold">}</span>
</pre>