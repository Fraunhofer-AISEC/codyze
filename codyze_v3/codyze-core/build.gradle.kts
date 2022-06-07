plugins {
    application
    id("codyze.core-conventions")
}

dependencies {
    // Code Property Graph
    api(libs.cpg.core)
    api(libs.cpg.analysis)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)

    // For (de)-serialization of SARIF and other files
    implementation(libs.kotlinx.serialization.json)
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

application {
    mainClass.set("MainKt")
}