plugins {
    id("documented-module")
}

dependencies {
    implementation(projects.codyzeCore)
    api(projects.codyzeSpecificationLanguages.coko.cokoCore)
    implementation(projects.codyzeSpecificationLanguages.coko.cokoExtensions)

    implementation(libs.kotlin.reflect)

    implementation(libs.sarif4k)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.host)

    implementation(kotlin("scripting-compiler-impl-embeddable") as String) {
        isTransitive = false
    }

    testImplementation(libs.mockk)
}
