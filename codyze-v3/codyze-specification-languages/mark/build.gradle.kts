plugins {
    id("documented-module")
}

dependencies {
    implementation(projects.codyzeCore)

    implementation(libs.bundles.cpg)
    implementation(libs.sarif4k)

    // weighted pushdown systems
    //api("de.breakpointsec:pushdown:1.1") // TODO find maintained alternative

    // MARK DSL (use fat jar). changing=true circumvents gradle cache
    //api("de.fraunhofer.aisec.mark:de.fraunhofer.aisec.mark:1.4.0-SNAPSHOT:repackaged") { isChanging = true } // ok
    //api("com.github.Fraunhofer-AISEC.codyze-mark-eclipse-plugin:de.fraunhofer.aisec.mark:bbd54a7b11:repackaged") // pin to specific commit before annotations
    implementation("com.github.Fraunhofer-AISEC.codyze-mark-eclipse-plugin:de.fraunhofer.aisec.mark:2.0.0:repackaged") // use GitHub release via JitPack

    // Codyze v2 MARK evaluation
    implementation("com.github.Fraunhofer-AISEC:codyze:main-SNAPSHOT") // use GitHub release via JitPack

    // TODO remove after moving to Kotlin
    // Reflections for OverflowDB and registering Crymlin built-ins
    //implementation("org.reflections", "reflections", "0.10.2")

    // TODO exchange with module
    // LSP interface support
    //api("org.eclipse.lsp4j:org.eclipse.lsp4j:0.12.0") // ok
}

tasks {
    jar {
        manifest {
            attributes("Implementation-Title" to "Codyze v3 - Specification Language MARK")
        }
    }
}
