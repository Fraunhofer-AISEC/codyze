plugins {
    id("documented-module")
}

dependencies {
    //implementation(libs.bundles.cpg)
    implementation("com.github.Fraunhofer-AISEC:cpg:5.0.0-alpha.2")

    implementation(libs.kotlin.reflect)
}
