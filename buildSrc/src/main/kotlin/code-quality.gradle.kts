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
        licenseHeaderFile("$rootDir/buildSrc/src/main/resources/licenseHeader.txt")
        targetExclude("**/*.codyze.kts")
    }
}