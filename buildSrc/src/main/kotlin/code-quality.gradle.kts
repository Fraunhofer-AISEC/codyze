import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.gradle.spotless.SpotlessCheck

plugins {
    kotlin("jvm")
    jacoco
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
    id("org.jmailen.kotlinter")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test) // tests are required to run before generating the report
}

val header = """
/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
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

// use kotlinter until spotless
// supports passing the .editorconfig to ktlint
// see: https://github.com/diffplug/spotless/issues/142
tasks.withType<SpotlessCheck> {
    dependsOn(tasks.lintKotlin)
}
tasks.withType<SpotlessApply> {
    dependsOn(tasks.formatKotlin)
}
// so for now we use spotless just for the license headers
spotless {
    kotlin {
        licenseHeader(header).yearSeparator(" - ")
    }
}