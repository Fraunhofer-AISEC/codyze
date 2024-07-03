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
package de.fraunhofer.aisec.codyze.plugins.compiled

import de.fraunhofer.aisec.codyze.plugins.PluginTest
import java.nio.file.Path
import kotlin.io.path.toPath
import kotlin.test.assertNotNull

abstract class CompiledPluginTest : PluginTest() {
    override fun scanFiles() {
        val libPath = PluginTest::class.java.classLoader.getResource("targets/libs/demo-cloud-service-1.0.0.jar")!!.toURI().toPath()
        val contextPaths = listOf(
            PluginTest::class.java.classLoader.getResource("targets/libs/bcpkix-jdk18on-1.75.jar")!!.toURI().toPath(),
            PluginTest::class.java.classLoader.getResource("targets/libs/bcprov-jdk18on-1.75.jar")!!.toURI().toPath(),
            PluginTest::class.java.classLoader.getResource("targets/libs/bctls-jdk18on-1.75.jar")!!.toURI().toPath(),
            PluginTest::class.java.classLoader.getResource("targets/libs/bcutil-jdk18on-1.75.jar")!!.toURI().toPath()
        )
        assertNotNull(libPath)

        plugin.execute(
            listOf(libPath),
            contextPaths,
            libPath.parent.parent.parent.resolve("generatedReports").resolve(resultFileName).toFile()
        )
    }
}
