plugins {
    kotlin("jvm")
}

group = "de.fraunhofer.aisec"

// configure shared attributes for JAR file manifests
tasks.jar {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Fraunhofer AISEC",
            "Implementation-Title" to project.name,
        )
    }
}