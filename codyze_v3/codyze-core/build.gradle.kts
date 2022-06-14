plugins {
    application
    id("codyze.core-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // implementation(projects.codyzeSpecificationLanguages.mark)  // TODO: re-enable

    // Code Property Graph
    api(libs.bundles.cpg)

    implementation(libs.clikt)

    implementation(libs.koin)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)

    // For (de)-serialization of SARIF and other files
    implementation(libs.kotlinx.serialization.json)
}
