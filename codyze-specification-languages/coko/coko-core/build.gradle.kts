plugins {
    id("documented-module")
}

dependencies {
    api(projects.codyzeCore)

    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.sarif)

    testImplementation(libs.mockk)
}
