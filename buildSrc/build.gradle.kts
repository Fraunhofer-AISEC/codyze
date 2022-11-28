plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.dokka.gradle)
    implementation(libs.sonarqube.gradle)
    implementation(libs.spotless.gradle)
    implementation(libs.license.gradle)
    implementation(libs.detekt.gradle)
    implementation(libs.kotlinter.gradle)

    // this is only there to be able to import 'LibrariesForLibs' in the convention plugins
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // additional dependencies used by plugins, e.g. for configuring tasks
    implementation(libs.dokka.base)
}
