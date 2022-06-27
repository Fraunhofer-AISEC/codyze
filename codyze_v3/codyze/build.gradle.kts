plugins {
    application
    id("codyze.core-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.mark)

    implementation(libs.clikt)
    implementation(libs.koin)

    // For deserialization of config files
    implementation(libs.kotlinx.serialization.json)
}

application {
    mainClass.set("de.fraunhofer.aisec.codyze.MainKt")
}