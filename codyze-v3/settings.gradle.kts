rootProject.name = "codyze-v3"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("codyze")
include("codyze-core")

include(":codyze-specification-languages:mark")
include(":codyze-specification-languages:coko:nwt-example")
//include(":codyze-specification-languages:coko:coko-core")
//include(":codyze-specification-languages:coko:coko-dsl")
//include(":codyze-specification-languages:coko:coko-extensions")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")
