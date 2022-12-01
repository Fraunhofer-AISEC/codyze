import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("metadata")
    id("code-quality")
    kotlin("jvm")
    idea
}

val libs = the<LibrariesForLibs>()
dependencies {
    // Logging
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.log4j.impl)

    // Unit tests
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.params)
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

    maven {
        setUrl("https://jitpack.io")
    }

    // Eclipse CDT repo --> needed for CPG
    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/10.3/cdt-10.3.2/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers"
        )
        // allWarningsAsErrors = true
    }
}

tasks.test {
    useJUnitPlatform()
}