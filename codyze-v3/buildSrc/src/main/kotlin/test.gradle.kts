import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    kotlin("jvm")
}

val libs = the<LibrariesForLibs>()
dependencies {
    // Unit tests
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.params)
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}
