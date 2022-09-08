allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = System.getProperty("version") ?: "0.0.0-SNAPSHOT"
    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

plugins {
    id("documented")
    id("base")
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
