allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = if(version != Project.DEFAULT_VERSION) version else "0.0.0-SNAPSHOT"
}

plugins {
    id("documented")
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(projectDir.resolve("..").resolve("docs").resolve("api").resolve("codyze-v3"))
}
