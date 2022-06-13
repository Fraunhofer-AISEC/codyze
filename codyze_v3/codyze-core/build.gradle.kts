plugins {
    application
    id("codyze.core-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.codyzeCommon)
    // implementation(projects.codyzeSpecificationLanguages.mark)  // TODO: re-enable

    // Code Property Graph
    api(libs.bundles.cpg)

    implementation(libs.clikt)

    implementation(libs.koin)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)

    // For (de)-serialization of SARIF and other files
    implementation(libs.kotlinx.serialization.json)

    // For parsing the configurations
//    implementation(libs.jackson.yaml)
//    implementation(libs.picocli)

    // For generating a json schema for the configurations
//    implementation(libs.jsonschema.generator)
//    implementation(libs.jsonschema.generator.jackson)
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
    mainClass.set("de.fraunhofer.aisec.codyze_core.MainKt")
}