import io.gitlab.arturbosch.detekt.Detekt
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

// this just generates a report for a single submodule
// a combined report is generated using the 'code-coverage-report' submodule
tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test) // tests are required to run before generating the report
}

// not using the detekt extension 'detekt { }' because with this
// it is possible to configure the custom 'detektFix' task as well
// in the main rootDir/build.gradle.kts, the reports for each submodule are combined into
// a merged report
tasks.withType<Detekt>().configureEach {
    basePath = "${rootProject.projectDir.absolutePath}"
    config.setFrom(files("$rootDir/detekt.yml"))
}

// custom task for fixing formatting issues
val detektFix by tasks.registering(Detekt::class) {
    description = "Run detekt and auto correct formatting issues."
    autoCorrect = true
    ignoreFailures = true
    setSource(files(projectDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
}

// for now we use spotless just for the license headers
spotless {
    kotlin {
        licenseHeaderFile("$rootDir/buildSrc/src/main/resources/licenseHeader.txt")
        targetExclude("**/*.codyze.kts")
    }
}