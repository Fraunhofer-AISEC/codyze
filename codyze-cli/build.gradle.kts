plugins {
    id("documented-module")
    application
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeBackends.cpg)
    implementation(projects.codyzeSpecificationLanguages.coko.cokoDsl)

    implementation(libs.clikt)
    implementation(libs.koin)
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.sarif)

    // For deserialization of config files
    implementation(libs.kotlinx.serialization.json)

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
    applicationName = "codyze"
}
