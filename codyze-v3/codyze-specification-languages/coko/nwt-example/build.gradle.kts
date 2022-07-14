plugins {
    id("documented-module")
}

dependencies {
    implementation(projects.codyzeCore)

    implementation(libs.sarif4k)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.host)
}
