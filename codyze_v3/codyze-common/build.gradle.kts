plugins {
    id("codyze.core-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)

    // Code Property Graph
    api(libs.bundles.cpg)

    implementation(libs.clikt)

    testImplementation(kotlin("test"))
}

repositories {
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