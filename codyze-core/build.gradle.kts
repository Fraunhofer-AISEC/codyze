plugins {
    id("documented-module")
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.clikt)

    implementation(libs.koin)

    implementation(libs.bundles.sarif)
}

// run the 'projectProps' task when the processResources task is run
// this makes sure that the 'projectProps' task is executed when building Codyze
tasks.processResources {
    from(rootProject.tasks.named("projectProps"))
}
