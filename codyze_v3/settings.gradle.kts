rootProject.name = "codyze_v3"

include("codyze-core")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")

// include all sub-modules in the 'codyze-specification_languages' folder
//file("codyze-specification_languages").listFiles()?.filter { it.isDirectory }?.forEach { dir: File ->
//    include(dir.name)
//    project(":${dir.name}").projectDir = dir
//}