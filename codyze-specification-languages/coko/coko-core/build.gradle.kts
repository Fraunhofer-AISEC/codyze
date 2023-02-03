plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    api(projects.codyzeCore)

    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.sarif)

    testImplementation(libs.mockk)
}

publishing {
    publications {
        named<MavenPublication>(name) {
            pom {
                name.set("Codyze Specification Language CoKo Core Library")
                description.set("Core library of CoKo specification language for Codyze")
            }
        }
    }
}
