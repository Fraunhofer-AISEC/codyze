plugins {
    id("documented-module")
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.clikt)

    implementation(libs.koin)

    // SARIF models
    // The code can be found here: https://github.com/detekt/sarif4k
    // The code in it was generated using https://app.quicktype.io/ with minor manual additions
    implementation(libs.sarif4k)
}

// run the 'projectProps' task when the processResources task is run
// this makes sure that the 'projectProps' task is executed when building Codyze
tasks.processResources {
    from(rootProject.tasks.named("projectProps"))
}