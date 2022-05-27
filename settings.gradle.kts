rootProject.name = "codyze"

includeBuild("src/codyze_legacy")

file("modules").listFiles()?.forEach { moduleBuild: File ->
    includeBuild(moduleBuild)
}

// TODO: move this into the codyze_legacy build
gradle.startParameter.excludedTaskNames += ":codyze_legacy:licenseMain"
gradle.startParameter.excludedTaskNames += ":codyze_legacy:licenseTest"