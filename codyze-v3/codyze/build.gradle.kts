plugins {
    id("documented-module")
    application
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.mark)
    implementation(projects.codyzeSpecificationLanguages.coko.nwtExample)

    implementation(libs.clikt)
    implementation(libs.koin)
    implementation(libs.kotlin.reflect)

    // For deserialization of config files
    implementation(libs.kotlinx.serialization.json)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)
}

application {
    mainClass.set("de.fraunhofer.aisec.codyze.MainKt")
}
