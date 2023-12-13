plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    // FIXME conflicts in dependencies!!
    //  e.g. both Spotbugs and PMD depend on Saxon-HE, so package signature does not match when PMD tries to call it
    implementation(libs.sarif4k)
    // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs
    implementation("com.github.spotbugs:spotbugs:4.8.2")
    // https://mvnrepository.com/artifact/net.sourceforge.pmd/
    implementation("net.sourceforge.pmd:pmd-core:7.0.0-rc4")
    implementation("net.sourceforge.pmd:pmd-java:7.0.0-rc4")
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