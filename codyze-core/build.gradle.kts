plugins {
    id("documented-module")
    id("publish")
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.clikt)

    implementation(libs.koin)

    implementation(libs.bundles.sarif)
}

publishing {
    publications {
        create<MavenPublication>("CodyzeCore") {
            from(components["java"])
            artifact(dokkaHtmlJar) // docs generated with Dokka
            pom {
                name.set("Codyze Core")
                description.set("Core library for Codyze")
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

// run the 'projectProps' task when the processResources task is run
// this makes sure that the 'projectProps' task is executed when building Codyze
tasks.processResources {
    from(rootProject.tasks.named("projectProps"))
}
