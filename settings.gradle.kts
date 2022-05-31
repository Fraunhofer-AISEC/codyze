rootProject.name = "codyze-composite"

includeBuild("codyze_v2")
includeBuild("codyze_v3")

// TODO: move this into the codyze_legacy build
gradle.startParameter.excludedTaskNames += ":codyze_v2:licenseMain"
gradle.startParameter.excludedTaskNames += ":codyze_v2:licenseTest"