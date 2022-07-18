tasks.register("build") {
    dependsOn(gradle.includedBuild("codyze-v2").task(":build"))
}