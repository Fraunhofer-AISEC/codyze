plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    implementation(libs.sarif4k)

    // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs
    implementation("com.github.spotbugs:spotbugs:4.8.2")
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