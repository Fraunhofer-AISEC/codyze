package de.fraunhofer.aisec.codyze.plugins.executor

import java.io.File
import java.nio.file.Path
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PmdAnalysis

class PMDExecutor: Executor {
    override fun execute(target: List<Path>, output: File) {
        val config = PMDConfiguration()
        for (path in target) {
            config.addInputPath(path)
        }
        config.reportFormat = "sarif"
        config.setReportFile(output.toPath())
        config.isIgnoreIncrementalAnalysis = true

        // from https://github.com/pmd/pmd/tree/master/pmd-core/src/main/resources/
        config.addRuleSet("src/main/resources/pmd-rulesets/all-java.xml")

        val analysis = PmdAnalysis.create(config)
        analysis.performAnalysis()
    }
}