import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

plugins {
    id("org.jetbrains.dokka")
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets {
        configureEach {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                // ensure we're including path segment to module
                remoteUrl.set(URL("https://github.com/Fraunhofer-AISEC/codyze/blob/main/codyze-v3/${project.projectDir.relativeTo(project.rootDir)}/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

repositories {
    mavenCentral()
}
