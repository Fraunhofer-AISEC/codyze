import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = if (version != Project.DEFAULT_VERSION) version else "0.0.0-SNAPSHOT"
}

plugins {
    id("documented")
}

// generate API documentation for website
tasks.dokkaHtmlMultiModule.configure {
    // add API docs to directory used for website generation
    outputDirectory.set(
        project.rootDir.resolve("..").resolve("docs").resolve("api").resolve("codyze-v3")
    )
    // path to Dokka assets
    val dokkaAssetsBaseDirectory =
        project.rootDir.resolve("..").resolve("docs").resolve("assets").resolve("dokka")
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
