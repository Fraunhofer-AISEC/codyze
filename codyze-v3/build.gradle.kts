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

subprojects {
    tasks {
        val projectProps by registering(WriteProperties::class) {
            description = "Write project properties in a file."

            // Set output file to build/project.properties
            outputFile = file("${buildDir}/project.properties")
            // Default encoding is ISO-8559-1, here we change it.
            encoding = "UTF-8"
            // Optionally we can specify the header comment.
            comment = "Version and name of project"

            // Define property.
            property("project.version", project.version)

            // Define properties using a Map.
            properties(mapOf("project.name" to project.name))
        }

    }



}