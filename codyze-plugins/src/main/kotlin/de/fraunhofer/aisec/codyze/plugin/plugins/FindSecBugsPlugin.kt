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
package de.fraunhofer.aisec.codyze.plugin.plugins

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

class FindSecBugsPlugin: de.fraunhofer.aisec.codyze.plugin.plugins.Plugin() {
    override val cliName = "findsecbugs"
    val pluginFile = File("src/main/resources/spotbugs-plugins/findsecbugs-plugin-1.12.0.jar")

    // NOTE: this Executor will very likely mark the invocation as failed
    // because of an (erroneous) missing class warning
    // see: https://github.com/find-sec-bugs/find-sec-bugs/issues/692
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
        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance())
        findbugs.userPreferences = UserPreferences.createDefaultUserPreferences()
        findbugs.userPreferences.enableAllDetectors(true)
        findbugs.execute()
    }
}