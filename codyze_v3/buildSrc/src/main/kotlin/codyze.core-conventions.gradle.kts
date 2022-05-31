import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    jacoco
    idea

    id("org.sonarqube")
    id("com.diffplug.spotless")
    // id("com.github.hierynomus.license")
}

group = "de.fraunhofer.aisec"
version = "1.0-SNAPSHOT"

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    // NOTE: Remove this when we "release". this is the public sonatype repo which is used to
    // sync to maven central, but directly adding this repo is faster than waiting for a maven central sync,
    // which takes 8 hours in the worse case. so it is useful during heavy development but should be removed
    // if everything is more stable
    maven {
        setUrl("https://oss.sonatype.org/content/groups/public")
    }
}

dependencies {
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.2")

    // Unit tests
    testImplementation(kotlin("test"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

// state that JSON schema parser must run before compiling Kotlin
//tasks.named("compileKotlin") {
//    dependsOn(":spotlessApply")
//}

tasks.named("sonarqube") {
    dependsOn(":jacocoTestReport")
}

spotless {
    kotlin {
        ktfmt().kotlinlangStyle()
    }
}

tasks.test {
    useJUnitPlatform()
}
