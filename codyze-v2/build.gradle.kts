import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // built-in
    java
    application
    jacoco
    idea
    `maven-publish`
    `java-library`

    id("org.jsonschema2dataclass") version "4.5.0"

    id("org.sonarqube") version "3.5.0.2730"
    id("com.diffplug.spotless") version "6.11.0"
    id("com.github.hierynomus.license") version "0.16.1"

    // documentation
    id("org.jetbrains.dokka") version "1.7.20"

    kotlin("jvm") version "1.7.20"
}

// Allow access to Dokka configuration for custom assets and stylesheets
buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.7.20")
    }
}

group = "de.fraunhofer.aisec"

/* License plugin needs a special treatment, as long as the main project does not have a license yet.
 * See https://github.com/hierynomus/license-gradle-plugin/issues/155
 */
// TODO this does not affect the build when built in a composite build
// TODO see the composite root settings.gradle.kts which disables these tasks for now
gradle.startParameter.excludedTaskNames += ":codyze-v2:licenseMain"
gradle.startParameter.excludedTaskNames += ":codyze-v2:licenseTest"

tasks {
    jar {
        manifest {
            attributes(
                "Implementation-Title" to "codyze",
                "Implementation-Version" to archiveVersion.getOrElse("0.0.0-dev")
            )
        }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        setUrl("https://jitpack.io")
    }

    // NOTE: Remove this when we "release". this is the public sonatype repo which is used to
    // sync to maven central, but directly adding this repo is faster than waiting for a maven
    // central sync, which takes 8 hours in the worse case. so it is useful during heavy
    // development but should be removed if everything is more stable
    maven {
        setUrl("https://oss.sonatype.org/content/groups/public")
    }

    // Eclipse CDT repo
    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/10.3/cdt-10.3.2/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }

    maven {
        setUrl("https://jitpack.io")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}

dependencies {
    // Logging
    api("org.slf4j:slf4j-api:2.0.3") // e.g., exposed by de.fraunhofer.aisec.codyze.analysis.markevaluation.Evaluator
    runtimeOnly("org.slf4j:log4j-over-slf4j:2.0.3") {
        because("Needed for `xtext.parser.antlr`")
    }
    implementation("org.apache.logging.log4j:log4j-core:2.19.0") // used by main and test
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")

    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect")) // pull in explicitly to prevent mixing versions

    // Code Property Graph
    api("de.fraunhofer.aisec:cpg-core:4.6.3")
    implementation("de.fraunhofer.aisec:cpg-analysis:4.6.3")

    // MARK DSL; use GitHub release via JitPack; e.g. exposed by de.fraunhofer.aisec.cpg.analysis.fsm.FSMBuilder
    api("com.github.Fraunhofer-AISEC.codyze-mark-eclipse-plugin:de.fraunhofer.aisec.mark:2.0.0:repackaged")

    // Pushdown Systems
    api("de.breakpointsec:pushdown:1.1") // e.g., exposed in de.fraunhofer.aisec.codyze.analysis.wpds

    // LSP interface support
    api("org.eclipse.lsp4j:org.eclipse.lsp4j:0.17.0") // e.g., exposed in de.fraunhofer.aisec.codyze.crymlin.connectors.lsp

    // Interactive console interface support using Jython (Scripting engine)
    implementation("org.python:jython-standalone:2.7.3") // ok

    // Command line interface support
    api("info.picocli:picocli:4.7.0") // e.g., exposed by de.fraunhofer.aisec.codyze.ManifestVersionProvider
    annotationProcessor("info.picocli:picocli-codegen:4.7.0")

    // Reflections for OverflowDB and registering Crymlin built-ins
    implementation("org.reflections:reflections:0.10.2")

    // Parser for yaml configuration file
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.4")

    // Unit tests
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
}

application {
    mainClass.set("de.fraunhofer.aisec.codyze.Main")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "6000m"
}

// Add generated sources from annotation processor to source sets so they can be picked up
sourceSets.configureEach {
    tasks.named<JavaCompile>(compileJavaTaskName) {
        java.srcDir(options.generatedSourceOutputDirectory.get())
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}

// Added mark files from dist folder to test resources
sourceSets.getByName("test").resources {
    srcDir("src/dist")
}

tasks.named("compileJava") {
    dependsOn(":spotlessApply")
}

// state that JSON schema parser must run before compiling Kotlin
tasks.named("compileKotlin") {
    dependsOn(":generateJsonSchema2DataClass")
}

tasks.named("sonarqube") {
    dependsOn(":jacocoTestReport")
}

spotless {
    java {
        // exclude automatically generated files
        targetExclude("build/generated/**/*.java")
        eclipse().configFile(rootProject.file("formatter-settings.xml"))
    }
    kotlin {
        ktfmt().kotlinlangStyle()
    }
}

downloadLicenses {
    includeProjectDependencies = true
    dependencyConfiguration = "compileClasspath"
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        /* On Windows the classpath can be too long. This is a workaround for https://github.com/gradle/gradle/issues/1989
         *
         * The problem doesn't seem to exist in the current version, but may reappear, so we're keeping this one around.
         */
        windowsScript.writeText(
            windowsScript
                .readText()
                .replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*")
        )
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

jsonSchema2Pojo {
    source.setFrom("src/main/resources/json")
    targetPackage.set("de.fraunhofer.aisec.codyze.sarif.schema")
    removeOldOutput.set(true)
    // ... more options
}

// generate API documentation
tasks.dokkaHtml.configure {
    // add API docs to directory used for website generation
    outputDirectory.set(
        project.rootDir.resolve("..").resolve("docs").resolve("api").resolve("codyze-v2")
    )
    // path to Dokka assets
    val dokkaAssetsBaseDirectory =
        project.rootDir.resolve("..").resolve("docs").resolve("assets").resolve("dokka")
    // configure custom assets
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        // use custom stylesheets without external content
        customStyleSheets =
            listOf(
                dokkaAssetsBaseDirectory.resolve("style.css"),
                dokkaAssetsBaseDirectory.resolve("jetbrains-mono.css"),
            )
    }
    // copy over font files
    dokkaAssetsBaseDirectory
        .resolve("JetBrainsMono")
        .copyRecursively(
            target = outputDirectory.get().resolve("styles").resolve("JetBrainsMono"),
            overwrite = true,
        )
    dokkaAssetsBaseDirectory
        .resolve("inter")
        .copyRecursively(
            target = outputDirectory.get().resolve("styles").resolve("inter"),
            overwrite = true,
        )
}
