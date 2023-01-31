plugins {
    id("documented-module")
    `maven-publish`
}

dependencies {
    api(projects.codyzeCore)

    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.sarif)

    testImplementation(libs.mockk)
}

// Configuration of JavaDoc task using Dokka taken from: https://stackoverflow.com/a/66714990
val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val dokkaHtmlJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications {
        create<MavenPublication>("CodyzeSpecLangCokoCore") {
            from(components["java"])
            artifact(dokkaHtmlJar) // docs generated with Dokka
            pom {
                name.set("Codyze Specification Language CoKo Core Library")
                description.set("Core library of CoKo specification language for Codyze")
                url.set("https://www.codyze.io/")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
