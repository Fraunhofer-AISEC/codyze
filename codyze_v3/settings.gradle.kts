rootProject.name = "codyze_v3"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("codyze-core")
include("codyze-common")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")

// TODO: re-enable once mark can be compiled
// include all submodules in the 'codyze-specification-languages' folder
//file("codyze-specification-languages").listFiles()?.filter{ it.isDirectory }?.forEach { dir: File ->
//    include(":codyze-specification-languages:${dir.name}")
//    project(":codyze-specification-languages:${dir.name}").projectDir = dir
//}
