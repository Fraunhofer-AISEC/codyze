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

import edu.umd.cs.findbugs.BugReporter
import edu.umd.cs.findbugs.DetectorFactoryCollection
import edu.umd.cs.findbugs.FindBugs2
import edu.umd.cs.findbugs.Plugin.loadCustomPlugin
import edu.umd.cs.findbugs.Project
import edu.umd.cs.findbugs.config.UserPreferences
import edu.umd.cs.findbugs.sarif.SarifBugReporter
import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.nio.file.Path

class FindSecBugsPlugin : Plugin("FindSecBugs") {
    /**
     * https://find-sec-bugs.github.io/download.htm
     * To update, download new Plugin versions and change jar name here.
     * When updating Plugins, make sure to update the documentation as well.
     */
    private val pluginFileURL: URL? =
        FindSecBugsPlugin::class.java.classLoader.getResource("spotbugs-plugins/findsecbugs-plugin-1.12.0.jar")

    // NOTE: this Executor will very likely mark the invocation as failed
    // because of an (erroneous) missing class warning
    // see: https://github.com/find-sec-bugs/find-sec-bugs/issues/692
    override fun execute(target: List<Path>, context: List<Path>, output: File) {
        val project = Project()

        for (t in target) {
            project.addFile(t.toString())
        }

        for (aux in context) {
            project.addAuxClasspathEntry(aux.toString())
        }

        val reporter = SarifBugReporter(project)
        output.createNewFile()
        reporter.setWriter(PrintWriter(output.writer()))
        reporter.setPriorityThreshold(BugReporter.NORMAL)

        pluginFileURL ?.run {
            val pluginFile = File(pluginFileURL.toURI())
            loadCustomPlugin(pluginFile, project)
        } ?: {
            logger.error { "Could not load FindSecBugs plugin from $pluginFileURL. Proceeding with default SpotBugs." }
        }

        val findbugs = FindBugs2()
        findbugs.bugReporter = reporter
        findbugs.project = project
        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance())
        findbugs.userPreferences = UserPreferences.createDefaultUserPreferences()
        findbugs.userPreferences.enableAllDetectors(true)
        findbugs.execute()
    }
}
