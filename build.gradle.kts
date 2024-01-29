import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    id("documented")
    id("code-quality")
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
            target = file(outputDirectory).resolve("styles").resolve("JetBrainsMono"),
            overwrite = true,
        )
    dokkaAssetsBaseDirectory
        .resolve("inter")
        .copyRecursively(
            target = file(outputDirectory).resolve("styles").resolve("inter"),
            overwrite = true,
        )
}

// adapted from https://blog.mrhaki.com/2021/03/gradle-goodness-create-properties-file.html
val projectProps by tasks.registering(WriteProperties::class) {
    description = "Write project properties in a file."

    // Set output file to build/project.properties
    destinationFile = file("${layout.buildDirectory}/codyze.properties")
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

// parse the Eclipse CDT version used in the CPG and applies it to Codyze
task("updateEclipseCDT") {
    doLast {
        val cpgVersion = libs.versions.cpg
        // If the location of the repository definition changes, change this url
        val url = "https://github.com/Fraunhofer-AISEC/cpg/blob/v${cpgVersion.get()}/buildSrc/src/main/kotlin/cpg.common-conventions.gradle.kts"
        val downloadedFile = layout.buildDirectory.file("cpg-conventions").get().asFile
        ant.invokeMethod("get", mapOf("src" to url, "dest" to downloadedFile))

        val newRegex = "setUrl\\(\\\\\"https:\\/\\/download\\.eclipse\\.org\\/tools\\/cdt\\/releases\\/.*\\/plugins\\\\\"\\)".toRegex()
        val matchResult = newRegex.find(downloadedFile.readText())

        // Include ALL files in codyze that define the CDT repository location
        val files = setOf(
            File("$rootDir/code-coverage-report/build.gradle.kts"),
            File("$rootDir/buildSrc/src/main/kotlin/module.gradle.kts")
        )

        if (matchResult != null) {
            val newRepo = matchResult.groups.first()!!.value.replace("\\", "")
            println("Setting Eclipse CDT version to '$newRepo'")
            val oldRegex = "setUrl\\(\\\"https:\\/\\/download\\.eclipse\\.org\\/tools\\/cdt\\/releases\\/.*\\/plugins\\\"\\)".toRegex()
            for (file in files) {
                val oldRepo = oldRegex.find(file.readText())?.groups?.first()?.value ?: continue
                file.writeText(file.readText().replace(oldRepo, newRepo))
            }
        } else {
            println("Eclipse CDT version info not found at '$url'")
        }
    }
}

// always run "updateEclipseCDT" before build
tasks.named("build") {
    dependsOn(tasks.named("updateEclipseCDT"))
}

// configure detekt to combine the results of all submodules into a single sarif file -> for github code scanning
val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/detekt.sarif"))
}

subprojects {
    plugins.withType<DetektPlugin> {
        tasks.withType<Detekt> detekt@{ // Sadly it has to be eager.
            finalizedBy(detektReportMergeSarif)

            detektReportMergeSarif.configure {
                input.from(this@detekt.sarifReportFile)
            }
        }
    }
}
