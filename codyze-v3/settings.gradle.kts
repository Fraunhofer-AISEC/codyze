rootProject.name = "codyze-v3"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

/*
 enable this if you want to develop codyze and the cpg together

 for this to work you have to slightly adapt the cpg project because of a bug in gradle concerning
 the "TYPESAFE_PROJECT_ACCESSORS" and submodules with the same name as the root project
 see: https://github.com/gradle/gradle/issues/16608
  - rename the cpg/cpg folder to cpg/cpg-main (or anything other than cpg really)
  - change all usages of the cpg/cpg folder to cpg/cpg-main:
      - in the cpg/cpg-main/build.gradle.kts file: replace "cpg" with "cpg-main"
      - in the cpg/settings.gradle.kts file: replace ":cpg" with ":cpg-main"
 */
// includeBuild("relative path to the cpg project")  // e.g., "../../cpg"

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
