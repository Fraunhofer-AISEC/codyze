plugins {
    id("documented-module")
    application
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.mark)

    implementation(libs.clikt)
    implementation(libs.koin)
    implementation(libs.kotlin.reflect)

    // For deserialization of config files
    implementation(libs.kotlinx.serialization.json)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)

    // For testing with koin
    // kotlin-test-junit has to be excluded because it is loaded by "documented-module" plugin
    testImplementation(libs.koin.test) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation(libs.koin.junit5){
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
}

application {
    mainClass.set("de.fraunhofer.aisec.codyze.MainKt")
}

tasks {
    jar {
        manifest {
            attributes("Implementation-Title" to "Codyze v3")
        }
    }
}

/**
 * Set version as system property when using run task, because there isn't a JAR file manifest.
 *
 * We already defined a sensible version in our parent project for all modules.
 */
tasks.named<JavaExec>("run") {
    systemProperty("codyze-v3-version", project.version)
}