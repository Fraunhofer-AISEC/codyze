plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    implementation(libs.sarif4k)
    // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs
    implementation("com.github.spotbugs:spotbugs:4.8.2")
    // https://mvnrepository.com/artifact/net.sourceforge.pmd/pmd-core
    implementation("net.sourceforge.pmd:pmd-core:6.55.0")
    // https://mvnrepository.com/artifact/net.sourceforge.pmd/pmd-java
    implementation("net.sourceforge.pmd:pmd-java:6.55.0")
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