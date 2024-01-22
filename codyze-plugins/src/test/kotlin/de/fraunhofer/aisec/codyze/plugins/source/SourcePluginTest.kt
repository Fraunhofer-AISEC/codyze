/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.plugins.source

import de.fraunhofer.aisec.codyze.plugins.PluginTest
import java.io.File
import java.nio.file.Path
import kotlin.test.assertNotNull

abstract class SourcePluginTest: PluginTest() {
    override fun scanFiles() {
        val sourcePath = PluginTest::class.java.classLoader.getResource("targets/TlsServer.java")?.path
        assertNotNull(sourcePath)

        plugin.execute(
            listOf(Path.of(sourcePath)),
            listOf(),
            File("src/test/resources/generatedReports/$resultFileName")
        )
    }
}