plugins {
    application
    id("codyze.core-conventions")
}

dependencies {
    implementation(projects.codyzeCore)
    implementation(projects.codyzeSpecificationLanguages.mark)

    implementation(libs.clikt)
    implementation(libs.koin)
}

application {
    mainClass.set("de.fraunhofer.aisec.codyze.MainKt")
}