import java.net.URL
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

// configure partial generation for multi module
tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets {
        configureEach {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                // ensure we're including path segment to module
                remoteUrl.set(
                    URL(
                        "https://github.com/Fraunhofer-AISEC/codyze/blob/main/codyze-v3/${project.projectDir.relativeTo(project.rootDir)}/src/main/kotlin"
                    )
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}

// configure task for individual module
tasks.withType<DokkaTask>().configureEach {
    // path to Dokka assets
    val dokkaAssetsBaseDirectory = project.rootDir.resolve("..").resolve("docs").resolve("assets").resolve("dokka")
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
