allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = if(version != "unspecified") version else "0.0.0-SNAPSHOT"
}

plugins {
    id("documented")
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
