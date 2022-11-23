plugins {
    id("documented-module")
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.clikt)

    implementation(libs.koin)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)
}

tasks {
    jar {
        manifest {
            attributes("Implementation-Title" to "Codyze v3 - Core Library")
        }
    }

    processResources {
        from(rootProject.tasks.named("projectProps"))
    }
}


