import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("codyze.core-conventions")
}

dependencies {
    testImplementation(kotlin("test"))

    // Code Property Graph
    api("de.fraunhofer.aisec:cpg-core:4.4.2")
    api("de.fraunhofer.aisec:cpg-analysis:4.4.2")
}

repositories {
    // Eclipse CDT repo
    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/10.3/cdt-10.3.2/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }

}

application {
    mainClass.set("MainKt")
}