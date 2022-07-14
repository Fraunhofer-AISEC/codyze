plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.dokka.gradle){
        exclude("org.jetbrains.kotlin")
    }  // this fixes https://github.com/Kotlin/dokka/issues/2546. TODO: Re-evaluate for Gradle 7.5!
    implementation(libs.sonarqube.gradle)
    implementation(libs.spotless.gradle)
    implementation(libs.license.gradle)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))  // this is only there to be able to import 'LibrariesForLibs' in the convention plugins
}
