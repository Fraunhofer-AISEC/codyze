plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.sonarqube.plugin)
    implementation(libs.spotless.plugin)
    implementation(libs.license.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
