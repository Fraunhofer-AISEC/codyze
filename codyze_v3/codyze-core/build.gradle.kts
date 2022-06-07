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

    // For parsing the configurations
    implementation(libs.jackson.yaml)
    implementation(libs.picocli)

    // For generating a json schema for the configurations
    implementation(libs.jsonschema.generator)
    implementation(libs.jsonschema.generator.jackson)
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

tasks.register("generateConfigSchema", JavaExec::class.java) {
    mainClass.set("de.fraunhofer.aisec.codyze_core.config.ConfigurationJsonSchemaGenerator")
    classpath = java.sourceSets["main"].runtimeClasspath
    args(rootDir)
}

application {
    mainClass.set("MainKt")
}