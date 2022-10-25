plugins {
    id("documented-module")
}

dependencies {
    api(projects.codyzeCore)

    implementation(libs.kotlin.reflect)

    testImplementation(libs.mockk)
}
