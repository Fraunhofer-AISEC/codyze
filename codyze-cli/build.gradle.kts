plugins {
    id("documented-module")
    application
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeBackends.cpg)
    implementation(projects.codyzeSpecificationLanguages.coko.cokoDsl)

    implementation(libs.clikt)
    implementation(libs.koin)
    implementation(libs.kotlin.reflect)

    // For deserialization of config files
    implementation(libs.kotlinx.serialization.json)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)

    // For testing with koin
    // kotlin-test-junit has to be excluded because it is loaded by "documented-module" plugin
    testImplementation(libs.koin.test) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation(libs.koin.junit5) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }

    testImplementation(libs.mockk)
}

application {
    mainClass.set("de.fraunhofer.aisec.codyze.cli.MainKt")
}
