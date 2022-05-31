plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.6.1")
    implementation("com.github.hierynomus.license:com.github.hierynomus.license.gradle.plugin:0.16.1")
}
