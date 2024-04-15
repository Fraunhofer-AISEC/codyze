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
package de.fraunhofer.aisec.codyze.plugins

import de.fraunhofer.aisec.codyze.core.plugin.Plugin
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PmdAnalysis
import org.koin.core.module.Module
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.writeBytes

class PMDPlugin : Plugin("PMD") {
    override fun execute(target: List<Path>, context: List<Path>, output: File) {
        val config = PMDConfiguration()
        for (path in target) {
            config.addInputPath(path)
        }
        config.reportFormat = "sarif"
        config.setReportFile(output.toPath())
        config.isIgnoreIncrementalAnalysis = true

        /**
         * From https://github.com/pmd/pmd/tree/master/pmd-core/src/main/resources/
         * When adding more rule sets, remember to update the documentation.
         *
         * PMD does not handle complex paths well, so we use the following workaround
         * TODO: let the user specify ruleset paths in the context
         */
        val ruleset = javaClass.classLoader.getResourceAsStream("pmd-rulesets/all-java.xml")?.readAllBytes()

        println("\n\n\n $ruleset \n \n\n\n")
        if (ruleset != null) {
            val tempRuleSet = kotlin.io.path.createTempFile()
            tempRuleSet.writeBytes(ruleset)
            config.addRuleSet(tempRuleSet.pathString)
        }

        val analysis = PmdAnalysis.create(config)
        analysis.performAnalysis()
    }

    override fun module(): Module = org.koin.dsl.module {
        factory { this@PMDPlugin } withOptions {
            named("de.fraunhofer.aisec.codyze.plugins.PMDPlugin")
        } bind (Plugin::class)
    }
}
