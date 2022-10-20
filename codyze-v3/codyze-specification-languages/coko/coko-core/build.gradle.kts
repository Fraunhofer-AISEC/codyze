plugins {
    id("documented-module")
}

dependencies {
    implementation(projects.codyzeCore)

    implementation(libs.kotlin.reflect)

    testImplementation(libs.mockk)
}
