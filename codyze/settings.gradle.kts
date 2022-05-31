rootProject.name = "codyze"

include("codyze-core")

// TODO re-enable modules once adapted to codyze v3
// include("codyze-lsp")
// include("codyze-console")

// include all sub-modules in the 'codyze-specification_languages' folder
//file("codyze-specification_languages").listFiles()?.filter { it.isDirectory }?.forEach { dir: File ->
//    include(dir.name)
//    project(":${dir.name}").projectDir = dir
//}

pluginManagement {
    val kotlin_version: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlin_version
    }
    val sonarcube_version: String by settings
    plugins {
        id("org.sonarqube") version sonarcube_version
    }
    val spotless_version: String by settings
    plugins {
        id("com.diffplug.spotless") version spotless_version
    }
    val license_version: String by settings
    plugins {
        id("com.github.hierynomus.license") version license_version
    }
}