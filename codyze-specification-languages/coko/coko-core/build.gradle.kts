plugins {
    id("documented-module")
}

dependencies {
    api(projects.codyzeCore)

    implementation(libs.kotlin.reflect)
    implementation(libs.sarif4k)

    testImplementation(libs.mockk)
}
