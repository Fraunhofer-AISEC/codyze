import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.gradle.spotless.SpotlessCheck
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("jvm")
    jacoco
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
}

val libs = the<LibrariesForLibs>()
dependencies {
    detektPlugins(libs.detekt.formatting)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test) // tests are required to run before generating the report
}

val header = """
/*
 * Copyright (c) ${"$"}YEAR, Fraunhofer AISEC. All rights reserved.
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
 *
 *     ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\                  ${'$'}${'$'}\
 *    ${'$'}${'$'}  __${'$'}${'$'}\                 ${'$'}${'$'} |
 *    ${'$'}${'$'} /  \__| ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\   ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'} |${'$'}${'$'}\   ${'$'}${'$'}\ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\  ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\
 *    ${'$'}${'$'} |      ${'$'}${'$'}  __${'$'}${'$'}\ ${'$'}${'$'}  __${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} |\____${'$'}${'$'}  |${'$'}${'$'}  __${'$'}${'$'}\
 *    ${'$'}${'$'} |      ${'$'}${'$'} /  ${'$'}${'$'} |${'$'}${'$'} /  ${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} |  ${'$'}${'$'}${'$'}${'$'} _/ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'} |
 *    ${'$'}${'$'} |  ${'$'}${'$'}\ ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} | ${'$'}${'$'}  _/   ${'$'}${'$'}   ____|
 *    \${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |\${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |\${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'} |\${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ \${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\
 *     \______/  \______/  \_______| \____${'$'}${'$'} |\________| \_______|
 *                                  ${'$'}${'$'}\   ${'$'}${'$'} |
 *                                  \${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |
 *                                   \______/
 *
 */
""".trimIndent()

// state that JSON schema parser must run before compiling Kotlin
tasks.named("compileKotlin") {
    dependsOn("spotlessApply")
}

detekt {
    config = files("$rootDir/detekt.yml")
}

val reportMerge by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/detekt.sarif"))
}

tasks.withType<Detekt> detekt@{ // Sadly it has to be eager.
    finalizedBy(reportMerge)

    reportMerge.configure {
        input.from(this@detekt.sarifReportFile)
    }
}

// for now we use spotless just for the license headers
spotless {
    kotlin {
        licenseHeader(header).yearSeparator(" - ")
        targetExclude("**/*.codyze.kts")
    }
}