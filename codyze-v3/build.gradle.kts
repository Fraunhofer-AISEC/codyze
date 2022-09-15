import org.jetbrains.kotlin.utils.keysToMap

allprojects {
    group = "de.fraunhofer.aisec.codyze"
    version = if (version != Project.DEFAULT_VERSION) version else "0.0.0-SNAPSHOT"
}

plugins {
    id("documented")
    kotlin
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(projectDir.resolve("..").resolve("docs").resolve("api").resolve("codyze-v3"))
}

// adapted from https://blog.mrhaki.com/2021/03/gradle-goodness-create-properties-file.html
val projectProps by tasks.registering(WriteProperties::class) {
    description = "Write project properties in a file."

    // Set output file to build/project.properties
    outputFile = file("${buildDir}/codyze.properties")
    // Default encoding is ISO-8559-1, here we change it.
    encoding = "UTF-8"
    // Optionally we can specify the header comment.
    comment = "Version of subprojects and name of project"

    // Add project name as property
    property("project.name", project.name)

    // Add version of each subproject as separate properties
    for(subproject in subprojects) {
        property("${subproject.name}.version", subproject.version)
    }
}