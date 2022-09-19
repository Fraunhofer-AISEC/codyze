rootProject.name = "codyze-v3"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

/*
 enable this if you want to develop codyze and the cpg together
 */
includeBuild("../../cpg")  // e.g., "../../cpg"

include("codyze")
include("codyze-core")

// MARK specification language
include(":codyze-specification-languages:mark")


// Codyze Kotlin specification language
include(":codyze-specification-languages:coko:coko-core")
include(":codyze-specification-languages:coko:coko-dsl")
include(":codyze-specification-languages:coko:coko-extensions")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")
