plugins {
    id("documented-module")
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.coko.cokoCore)

    implementation(libs.bundles.cpg)
    implementation(libs.clikt)
    implementation(libs.koin)

    testImplementation(libs.mockk)
}