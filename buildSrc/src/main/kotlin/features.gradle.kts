plugins {
    kotlin("jvm")
}

val enablePluginSupport: Boolean by rootProject.extra

dependencies {
    if (enablePluginSupport) runtimeOnly(project(":codyze-plugins"))
}