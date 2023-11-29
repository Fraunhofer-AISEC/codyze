plugins {
    id("documented-module")
    id("publish")

    // Analysis plugins
    id("com.github.spotbugs") version "6.0.0-rc.3"
}

dependencies {
    implementation(libs.sarif4k)
}

publishing {
    publications {
        named<MavenPublication>(name) {
            pom {
                name.set("Codyze External Results Aggregator")
                description.set("Aggregator for results produced by external analysis tools")
            }
        }
    }
}

spotbugs {
    reportsDir.set(file("$projectDir/codyze-plugins/src/main/resources/reports"))
    // TODO: can we even use such plugins to analyze external code?
    onlyAnalyze.set(listOf("com.external.*"))
    ignoreFailures.set(true)
}