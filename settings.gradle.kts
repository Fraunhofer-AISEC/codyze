rootProject.name = "codyze-composite"

includeBuild("codyze_legacy")
includeBuild("codyze")

file("modules").listFiles()?.forEach { moduleBuild: File ->
    includeBuild(moduleBuild)
}

// TODO: move this into the codyze_legacy build
gradle.startParameter.excludedTaskNames += ":codyze_legacy:licenseMain"
gradle.startParameter.excludedTaskNames += ":codyze_legacy:licenseTest"