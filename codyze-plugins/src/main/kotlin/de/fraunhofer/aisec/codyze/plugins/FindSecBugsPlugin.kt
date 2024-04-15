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
import de.fraunhofer.aisec.codyze.core.plugin.logger
import edu.umd.cs.findbugs.BugReporter
import edu.umd.cs.findbugs.DetectorFactoryCollection
import edu.umd.cs.findbugs.FindBugs2
import edu.umd.cs.findbugs.Plugin.loadCustomPlugin
import edu.umd.cs.findbugs.PluginException
import edu.umd.cs.findbugs.Project
import edu.umd.cs.findbugs.config.UserPreferences
import edu.umd.cs.findbugs.sarif.SarifBugReporter
import org.koin.core.module.Module
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
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
        if (output.parentFile != null) {
            output.parentFile.mkdirs()
        }
        reporter.setWriter(PrintWriter(output.writer()))
        reporter.setPriorityThreshold(BugReporter.NORMAL)

        // find and load Find Security Bugs plugin for SpotBugs
        logger.debug { "Trying to locate 'Find Security Bugs' plugin for SpotBugs" }
        val findSecBugsPlugin = javaClass.classLoader.getResources("findbugs.xml").toList().find { it.toString().contains("findsecbugs-plugin") }

        logger.info { "Found potential plugin location at $findSecBugsPlugin" }
        findSecBugsPlugin?.run {
            val pluginJar = Regex("^jar:file:(.*[.]jar)!/.*").replace(findSecBugsPlugin.toString(), "$1")

            logger.info { "Loading SpotBugs plugin 'Find Security Bugs' from JAR $pluginJar" }
            try {
                loadCustomPlugin(File(pluginJar), project)
            } catch (e: PluginException) {
                logger.warn { "Could not load FindSecBugs plugin from $pluginJar.\n$e" }
            }
        } ?: logger.warn { "Could not load FindSecBugs plugin from $findSecBugsPlugin. Proceeding with default SpotBugs." }

        val findbugs = FindBugs2()
        findbugs.bugReporter = reporter
        findbugs.project = project
        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance())
        findbugs.userPreferences = UserPreferences.createDefaultUserPreferences()
        findbugs.userPreferences.enableAllDetectors(true)
        findbugs.execute()
    }

    override fun module(): Module = org.koin.dsl.module {
        factory { this@FindSecBugsPlugin } withOptions {
            named("de.fraunhofer.aisec.codyze.plugins.FindSecBugsPlugin")
        } bind (Plugin::class)
    }
}
