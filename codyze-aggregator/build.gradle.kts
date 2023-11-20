plugins {
    id("documented-module")
    id("publish")
}

dependencies {
    // TODO: provide basic analysis Tools as libraries?
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