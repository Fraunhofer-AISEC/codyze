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
package de.fraunhofer.aisec.codyze.core

import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.util.Properties
import kotlin.test.assertEquals

class VersionProviderTest {

    @Test
    fun `test initialisation from properties file with incorrect project name`() {
        val properties = Properties()

        /* get the property file and load it; we want to fail if we can't find the property file to begin with */
        val propFile = VersionProvider::class.java.classLoader.getResource("codyze.properties")!!
        propFile.openStream().use {
            properties.load(it)
        }

        // change property s.t. internal check fails
        val oldValue = properties.setProperty("project.name", "test") as String
        File(propFile.toURI()).outputStream().use {
            properties.store(it, null)
        }

        // instantiate `VersionProvider` with altered properties -> properties in VersionProvider should now be empty
        val vp = VersionProvider

        // check empty properties through reflection
        val vpProps = vp.javaClass.getDeclaredField("props")
            .also { it.trySetAccessible() }
            .let { it.get(vp) as Properties }
        assertEquals(vpProps.size, 0)

        // restore original properties file
        properties.setProperty("project.name", oldValue)
        File(propFile.toURI()).outputStream().use {
            properties.store(it, null)
        }
    }
}
