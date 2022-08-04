allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = System.getProperty("version") ?: "0.0.0-SNAPSHOT"
}

plugins {
    id("documented")
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(projectDir.resolve("..").resolve("docs").resolve("assets").resolve("api").resolve("codyze-v3"))
}
