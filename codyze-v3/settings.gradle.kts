rootProject.name = "codyze-v3"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("codyze")
include("codyze-core")

// MARK specification language
include(":codyze-specification-languages:mark")


// Codyze Kotlin specification language
include(":codyze-specification-languages:coko:coko-core")
include(":codyze-specification-languages:coko:coko-dsl")
include(":codyze-specification-languages:coko:coko-extensions")

// Test for Kotlin specification language
include(":codyze-specification-languages:nwt-example")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")
