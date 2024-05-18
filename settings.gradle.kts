rootProject.name = "codyze"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

/*
 enable this if you want to develop codyze and the cpg together
 */
// includeBuild("../cpg")  // e.g., "../cpg"

include("code-coverage-report")
include("codyze-cli")
include("codyze-core")
include(":codyze-backends:cpg")

// Codyze Kotlin specification language
include(":codyze-specification-languages:coko:coko-core")
include(":codyze-specification-languages:coko:coko-dsl")

// Including the nativ cpg dsl queries
include(":codyze-specification-languages:cpg-native")

/*
 * Optional and experimental features
 */
// Support external plugins, e.g. code analysis tools
val enablePluginSupport: Boolean by extra {
    val enablePluginSupport: String? by settings
    enablePluginSupport.toBoolean()
}
if (enablePluginSupport) include(":codyze-plugins")
