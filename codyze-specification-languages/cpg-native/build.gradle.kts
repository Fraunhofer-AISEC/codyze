plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.coko.cokoCore)
    implementation(projects.codyzeBackends.cpg) // used only for the CokoScript plugin block configuration
    implementation(libs.bundles.cpg)
    implementation(libs.kotlin.reflect)

    implementation(libs.sarif4k)
    implementation(libs.koin)
    implementation(libs.clikt)

    // For testing with koin
    // kotlin-test-junit has to be excluded because it is loaded by "documented-module" plugin
    testImplementation(libs.koin.test) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation(libs.koin.junit5) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.cpg)
}

publishing {
    publications {
        named<MavenPublication>(name) {
            pom {
                name.set("Codyze Specification Language Native CPG Query DSL")
                description.set("Queries with native CPG DSL for Codyze")
            }
        }
    }
}
