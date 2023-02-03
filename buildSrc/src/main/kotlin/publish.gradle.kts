import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `java-library`
    `maven-publish`

    id("org.jetbrains.dokka")
}

java {
    //withJavadocJar() // using custom JavaDoc from Dokka; FIXME maybe there is a better way?
    withSourcesJar()
}

// Configuration of JavaDoc task using Dokka taken from: https://stackoverflow.com/a/66714990
val dokkaHtml by tasks.getting(DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

// default metadata for publications
publishing {
    publications {
        create<MavenPublication>(name) {
            from(components["java"])
            artifact(javadocJar)
            pom {
                url.set("https://www.codyze.io/")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Codyze Developer Team")
                        organization.set("Fraunhofer AISEC")
                        organizationUrl.set("https://www.aisec.fraunhofer.de/")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Fraunhofer-AISEC/codyze.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Fraunhofer-AISEC/codyze.git")
                    url.set("https://github.com/Fraunhofer-AISEC/codyze")
                }
            }
        }
    }
}