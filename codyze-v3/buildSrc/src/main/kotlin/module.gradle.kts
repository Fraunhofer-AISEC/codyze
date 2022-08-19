import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("jvm")
    jacoco
    idea

    id("org.sonarqube")
    id("com.diffplug.spotless")
}

// configure shared attributes for JAR file manifests
tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Fraunhofer AISEC",
            "Implementation-Vendor-Id" to project.group,
            "Implementation-URL" to "https://github.com/Fraunhofer-AISEC/codyze"
        )
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
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

    // Eclipse CDT repo
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
            "-opt-in=kotlin.RequiresOptIn"
        )
        allWarningsAsErrors = project.findProperty("warningsAsErrors") == "true"
    }
}

// state that JSON schema parser must run before compiling Kotlin
tasks.named("compileKotlin") {
    dependsOn("spotlessApply")
}

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
