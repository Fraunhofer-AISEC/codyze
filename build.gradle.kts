import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    id("documented")
    id("code-quality")
    alias(libs.plugins.sonarqube)
}

// generate API documentation for website
tasks.dokkaHtmlMultiModule.configure {
    // add API docs to directory used for website generation
    outputDirectory.set(
        project.rootDir.resolve("docs").resolve("api").resolve("codyze")
    )
    // path to Dokka assets
    val dokkaAssetsBaseDirectory =
        project.rootDir.resolve("docs").resolve("assets").resolve("dokka")
    // configure custom assets
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        // use custom stylesheets without external content
        customStyleSheets =
            listOf(
                dokkaAssetsBaseDirectory.resolve("style.css"),
                dokkaAssetsBaseDirectory.resolve("jetbrains-mono.css"),
            )
    }
    // copy over font files
    dokkaAssetsBaseDirectory
        .resolve("JetBrainsMono")
        .copyRecursively(
            target = outputDirectory.get().resolve("styles").resolve("JetBrainsMono"),
            overwrite = true,
        )
    dokkaAssetsBaseDirectory
        .resolve("inter")
        .copyRecursively(
            target = outputDirectory.get().resolve("styles").resolve("inter"),
            overwrite = true,
        )
}

// adapted from https://blog.mrhaki.com/2021/03/gradle-goodness-create-properties-file.html
val projectProps by tasks.registering(WriteProperties::class) {
    description = "Write project properties in a file."

    // Set output file to build/project.properties
    outputFile = file("$buildDir/codyze.properties")
    // Default encoding is ISO-8559-1, here we change it.
    encoding = "UTF-8"
    // Optionally we can specify the header comment.
    comment = "Version of subprojects and name of project"

    // Add project name as property
    property("project.name", project.name)

    // Add version of each subproject as separate properties
    for (subproject in subprojects) {
        property("${subproject.name}.version", subproject.version)
    }
}

// configure detekt to combine the results of all submodules into a single sarif file -> for github code scanning
val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/detekt.sarif"))
}
// configure detekt to combine the results of all submodules into a single xml file -> for sonarqube
val detektReportMergeXml by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/detekt.xml"))
}
subprojects {
    plugins.withType<DetektPlugin> {
        tasks.withType<Detekt> detekt@{ // Sadly it has to be eager.
            finalizedBy(detektReportMergeSarif, detektReportMergeXml)

            detektReportMergeSarif.configure {
                input.from(this@detekt.sarifReportFile)
            }
            detektReportMergeXml.configure {
                input.from(this@detekt.xmlReportFile)
            }
        }
    }
}

//
// Configure sonarqube for the whole codyze project
// TODO: does not work -> test codecov instead?
// sonarqube {
//    properties {
//        property("sonar.sourceEncoding", "UTF-8")
//        property("sonar.coverage.jacoco.xmlReportPaths", "${rootDir}/code-coverage-report/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")
//        property("sonar.kotlin.detekt.reportPaths", "${rootProject.buildDir}/reports/detekt/detekt.xml")
//    }
// }
