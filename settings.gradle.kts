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
include("codyze-aggregator")

// Codyze Kotlin specification language
include(":codyze-specification-languages:coko:coko-core")
include(":codyze-specification-languages:coko:coko-dsl")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")
include("codyze-aggregator")
