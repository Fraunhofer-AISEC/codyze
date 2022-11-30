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

// this just generates a report for a single submodule
// a combined report is generated using the 'code-coverage-report' submodule
tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test) // tests are required to run before generating the report
}

// in the main rootDir/build.gradle.kts, the reports for each submodule are combined into
// a merged report
detekt {
    config = files("$rootDir/detekt.yml")
    basePath = "${rootDir.absolutePath}"
}

// for now we use spotless just for the license headers
spotless {
    kotlin {
        licenseHeaderFile("$rootDir/buildSrc/src/main/resources/licenseHeader.txt")
        targetExclude("**/*.codyze.kts")
    }
}