plugins {
    id("documented-module")
    id("publish")
}

repositories {
    maven("https://dl.bintray.com/palantir/releases")
}

dependencies {
    implementation(libs.sarif4k)
    implementation(libs.clikt)
    implementation(libs.koin)

    implementation(projects.codyzeCore)

    /**
     * When updating Plugins, make sure to update the documentation as well.
     */
    // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs
    // it is necessary to exclude saxon because of conflicts with same transitive dependency in PMD
    implementation("com.github.spotbugs:spotbugs:4.8.5") {
        exclude(group = "net.sf.saxon", module = "Saxon-HE")
    }
    implementation("com.h3xstream.findsecbugs:findsecbugs-plugin:1.13.0")

    // https://mvnrepository.com/artifact/net.sourceforge.pmd/
    implementation("net.sourceforge.pmd:pmd-core:7.1.0")
    implementation("net.sourceforge.pmd:pmd-java:7.2.0")
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
