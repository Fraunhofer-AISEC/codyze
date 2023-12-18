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
// TODO: may be unnecessary after addition of FindSecBugsExecutor
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
        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance())
        findbugs.userPreferences = UserPreferences.createDefaultUserPreferences()
        findbugs.userPreferences.enableAllDetectors(true)
        findbugs.execute()
    }
}