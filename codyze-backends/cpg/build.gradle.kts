plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.coko.cokoCore)

    implementation(libs.bundles.cpg)
    implementation(libs.clikt)
    implementation(libs.koin)
    implementation(libs.bundles.sarif)

    testImplementation(libs.mockk)
}

publishing {
    publications {
        named<MavenPublication>(name) {
            pom {
                name.set("Codyze Backends CPG")
                description.set("CPG backend for Codyze")
            }
        }
    }
}
