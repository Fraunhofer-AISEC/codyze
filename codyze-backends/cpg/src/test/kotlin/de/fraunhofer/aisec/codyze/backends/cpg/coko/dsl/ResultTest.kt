/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ResultTest {
    @Test
    fun testIterableAll() {
        val allValid = listOf(Result.VALID, Result.VALID, Result.VALID)
        val oneInvalid = listOf(Result.VALID, Result.VALID, Result.INVALID)
        val oneOpen = listOf(Result.VALID, Result.VALID, Result.OPEN)
        val oneInvalidOneOpen = listOf(Result.VALID, Result.INVALID, Result.OPEN)

        val resultA = allValid.allResult { it }
        val resultB = oneInvalid.allResult { it }
        val resultC = oneOpen.allResult { it }
        val resultD = oneInvalidOneOpen.allResult { it }

        assertEquals(Result.VALID, resultA)
        assertEquals(Result.INVALID, resultB)
        assertEquals(Result.OPEN, resultC)
        assertEquals(Result.OPEN, resultD)
    }

    @Test
    fun testIterableAny() {
        val allValid = listOf(Result.VALID, Result.VALID, Result.VALID)
        val oneValid = listOf(Result.VALID, Result.OPEN, Result.INVALID)
        val oneOpen = listOf(Result.OPEN, Result.INVALID, Result.INVALID)
        val onlyInvalid = listOf(Result.INVALID, Result.INVALID, Result.INVALID)

        val resultA = allValid.anyResult { it }
        val resultB = oneValid.anyResult { it }
        val resultC = oneOpen.anyResult { it }
        val resultD = onlyInvalid.anyResult { it }

        assertEquals(Result.VALID, resultA)
        assertEquals(Result.VALID, resultB)
        assertEquals(Result.OPEN, resultC)
        assertEquals(Result.INVALID, resultD)
    }

    @Test
    fun testArrayAll() {
        val allValid = arrayOf(Result.VALID, Result.VALID, Result.VALID)
        val oneInvalid = arrayOf(Result.VALID, Result.VALID, Result.INVALID)
        val oneOpen = arrayOf(Result.VALID, Result.VALID, Result.OPEN)
        val oneInvalidOneOpen = arrayOf(Result.VALID, Result.INVALID, Result.OPEN)

        val resultA = allValid.allResult { it }
        val resultB = oneInvalid.allResult { it }
        val resultC = oneOpen.allResult { it }
        val resultD = oneInvalidOneOpen.allResult { it }

        assertEquals(Result.VALID, resultA)
        assertEquals(Result.INVALID, resultB)
        assertEquals(Result.OPEN, resultC)
        assertEquals(Result.OPEN, resultD)
    }

    @Test
    fun testArrayAny() {
        val allValid = listOf(Result.VALID, Result.VALID, Result.VALID)
        val oneValid = listOf(Result.VALID, Result.OPEN, Result.INVALID)
        val oneOpen = listOf(Result.OPEN, Result.INVALID, Result.INVALID)
        val onlyInvalid = listOf(Result.INVALID, Result.INVALID, Result.INVALID)

        val resultA = allValid.anyResult { it }
        val resultB = oneValid.anyResult { it }
        val resultC = oneOpen.anyResult { it }
        val resultD = onlyInvalid.anyResult { it }

        assertEquals(Result.VALID, resultA)
        assertEquals(Result.VALID, resultB)
        assertEquals(Result.OPEN, resultC)
        assertEquals(Result.INVALID, resultD)
    }

    @Test
    fun testResultAnd() {
        assertEquals(Result.VALID, Result.VALID.and(Result.VALID))
        assertEquals(Result.INVALID, Result.INVALID.and(Result.VALID))
        assertEquals(Result.INVALID, Result.VALID.and(Result.INVALID))
        assertEquals(Result.OPEN, Result.OPEN.and(Result.VALID))
        assertEquals(Result.OPEN, Result.VALID.and(Result.OPEN))
        assertEquals(Result.OPEN, Result.OPEN.and(Result.INVALID))
    }

    @Test
    fun testConvert() {
        assertEquals(Result.VALID, Result.convert(Result.VALID))
        assertEquals(Result.INVALID, Result.convert(Result.INVALID))
        assertEquals(Result.OPEN, Result.convert(Result.OPEN))
        assertEquals(Result.VALID, Result.convert(true))
        assertEquals(Result.INVALID, Result.convert(false))
        assertEquals(Result.OPEN, Result.convert("123"))
    }
}
