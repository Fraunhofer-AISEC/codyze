plugins {
    id("documented-module-test")
}

dependencies {
    // Code Property Graph
    api(libs.bundles.cpg)

    implementation(libs.kotlin.reflect)

    implementation(libs.clikt)

    implementation(libs.koin)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)
}
