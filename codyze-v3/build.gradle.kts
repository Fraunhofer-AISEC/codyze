allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = System.getProperty("version") ?: "0.0.0-SNAPSHOT"
}

plugins {
    id("documented")
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
