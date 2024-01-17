plugins {
    id("documented-module")
    id("publish")
}

repositories {
    maven("https://dl.bintray.com/palantir/releases")
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.sarif4k)
    implementation(projects.codyzeCore)

    // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs
    // it is necessary to exclude saxon because of conflicts with same transitive dependency in PMD
    implementation("com.github.spotbugs:spotbugs:4.8.2") {
        exclude(group = "net.sf.saxon", module = "Saxon-HE")
    }
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