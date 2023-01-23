plugins {
    id("documented-module")
}

dependencies {
    implementation(projects.codyzeCore)
    api(projects.codyzeSpecificationLanguages.coko.cokoCore)
    implementation(projects.codyzeBackends.cpg) // used only for the CokoScript plugin block configuration

    implementation(libs.kotlin.reflect)

    implementation(libs.sarif4k)
    implementation(libs.koin)
    implementation(libs.clikt)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.host)
    implementation(libs.kotlin.scripting.dependencies)

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
