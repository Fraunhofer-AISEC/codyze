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

import java.io.File
import java.nio.file.Path
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PmdAnalysis

class PMDPlugin: Plugin("PMD") {
    override fun execute(target: List<Path>, output: File) {
        val config = PMDConfiguration()
        for (path in target) {
            config.addInputPath(path)
        }
        config.reportFormat = "sarif"
        config.setReportFile(output.toPath())
        config.isIgnoreIncrementalAnalysis = true

        // from https://github.com/pmd/pmd/tree/master/pmd-core/src/main/resources/
        val ruleset = PMDPlugin::class.java.classLoader.getResource("pmd-rulesets/all-java.xml")
        if (ruleset != null)
            config.addRuleSet(ruleset.path)

        val analysis = PmdAnalysis.create(config)
        analysis.performAnalysis()
    }
}