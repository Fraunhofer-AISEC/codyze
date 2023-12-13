package de.fraunhofer.aisec.codyze.plugins.executor

import edu.umd.cs.findbugs.BugReporter
import edu.umd.cs.findbugs.DetectorFactoryCollection
import edu.umd.cs.findbugs.FindBugs2
import edu.umd.cs.findbugs.Plugin
import edu.umd.cs.findbugs.Project
import edu.umd.cs.findbugs.config.UserPreferences
import edu.umd.cs.findbugs.sarif.SarifBugReporter
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute

// FIXME: copy-paste from SpotBugs-Executor with added FindSecBugs-Plugin
class FindSecBugsExecutor: Executor {
    val pluginFile = File("src/main/resources/spotbugs-plugins/findsecbugs-plugin-1.12.0.jar")

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

        // TODO: automatically download new Plugin versions and change version number here!
        // https://find-sec-bugs.github.io/download.htm
        Plugin.loadCustomPlugin(pluginFile, project)

        val findbugs = FindBugs2()
        findbugs.bugReporter = reporter
        findbugs.project = project
        // trying to pass DetectorFactoryCollection(plugin) crashes the analysis
        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection())
        findbugs.userPreferences = UserPreferences.createDefaultUserPreferences()
        findbugs.userPreferences.enableAllDetectors(true)
        // this is doubled... should not be necessary
        findbugs.userPreferences.customPlugins = mapOf(pluginFile.absolutePath to true)
        findbugs.execute()
    }
}