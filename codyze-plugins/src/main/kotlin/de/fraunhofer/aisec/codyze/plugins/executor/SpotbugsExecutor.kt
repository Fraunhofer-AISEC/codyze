package de.fraunhofer.aisec.codyze.plugins.executor

import java.nio.file.Path
import edu.umd.cs.findbugs.*
import edu.umd.cs.findbugs.config.UserPreferences
import edu.umd.cs.findbugs.sarif.SarifBugReporter
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import kotlin.io.path.absolute

// see https://javadoc.io/doc/com.github.spotbugs/spotbugs/latest/edu/umd/cs/findbugs/package-summary.html
// NOTE: This Executor works on Java bytecode!
class SpotbugsExecutor: Executor {
    override fun execute(target: List<Path>, output: File) {
        val project = Project()
        // TODO: for now we assume all necessary libraries are close to the target
        for (t in target) {
            for (p in Files.walk(t.parent).map { it.absolute().toString() }.toList()) {
                project.addAuxClasspathEntry(p)
            }
            project.addFile(t.toString())
        }

        val reporter = SarifBugReporter(project)
        reporter.setWriter(PrintWriter(output.writer()))
        reporter.setPriorityThreshold(BugReporter.NORMAL)

        val findbugs = FindBugs2()
        findbugs.bugReporter = reporter
        findbugs.project = project
        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection())
        findbugs.userPreferences = UserPreferences.createDefaultUserPreferences()
        findbugs.execute()
    }


}