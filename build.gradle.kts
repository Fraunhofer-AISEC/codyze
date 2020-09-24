val deployUsername: String? by extra // imported from settings.gradle.kts
val deployPassword: String? by extra // imported from settings.gradle.kts

plugins {
    // built-in
    java
    application
    jacoco
    idea
    `maven-publish`
    `java-library`

    id("org.sonarqube") version "3.0"
    id("com.diffplug.spotless") version "5.6.1"
    id("com.github.hierynomus.license") version "0.15.0"
}


group = "de.fraunhofer.aisec"

/* License plugin needs a special treatment, as long as the main project does not have a license yet.
   See https://github.com/hierynomus/license-gradle-plugin/issues/155 
*/
gradle.startParameter.excludedTaskNames += "licenseMain"
gradle.startParameter.excludedTaskNames += "licenseTest"


tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
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

    // NOTE: Remove this when we "release". this is the public sonatype repo which is used to
    // sync to maven central, but directly adding this repo is faster than waiting for a maven central sync,
    // which takes 8 hours in the worse case. so it is useful during heavy development but should be removed
    // if everything is more stable
    maven {
        setUrl("https://oss.sonatype.org/content/groups/public")
    }

    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/9.11/cdt-9.11.1/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
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
    api("org.json:json:20200518")

    api("org.apache.logging.log4j:log4j-slf4j18-impl:2.13.3")
    api("org.apache.logging.log4j:log4j-core:2.13.3")
    api("org.slf4j:log4j-over-slf4j:1.8.0-beta4") // needed for xtext.parser.antlr
    api("org.slf4j:slf4j-api:1.8.0-beta4")

    api("com.github.javaparser:javaparser-symbol-solver-core:3.16.1")

    // Code Property Graph
    api("de.fraunhofer.aisec:cpg:2.3.0")

    // MARK DSL (use fat jar). changing=true circumvents gradle cache
     api("de.fraunhofer.aisec.mark:de.fraunhofer.aisec.mark:1.4.0-SNAPSHOT:repackaged") { setChanging(true) }

    // LSP
    api("org.eclipse.lsp4j:org.eclipse.lsp4j:0.9.0")

    // JSON parser for generation of results file
    api("org.json:json:20190722")

    // JsonPath for querying JSON
    api("com.jayway.jsonpath:json-path:2.4.0")

    // Command line interface support
    api("info.picocli:picocli:4.5.1")
    annotationProcessor("info.picocli:picocli-codegen:4.5.1")

    // Gremlin
    api("org.apache.tinkerpop:gremlin-core:3.4.3")
    annotationProcessor("org.apache.tinkerpop:gremlin-core:3.4.3") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.slf4j", module = "jcl-over-slf4j")
    }      // Newer Gradle versions require specific classpath for annotatation processors
    api("org.apache.tinkerpop:gremlin-python:3.4.3")
    api("org.apache.tinkerpop:tinkergraph-gremlin:3.4.3")
    api("org.apache.tinkerpop:gremlin-driver:3.4.3")
    api("org.apache.tinkerpop:neo4j-gremlin:3.4.3")     // Neo4j multi-label support for gremlin

    // Fast in-memory graph DB (alternative to Neo4J)
    api("io.shiftleft:overflowdb-tinkerpop3:0.128")
    api("org.reflections", "reflections", "0.9.11")

    // Pushdown Systems
    api("de.breakpointsec:pushdown:1.1")

    // Jython (Scripting engine)
    api("org.python:jython-standalone:2.7.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

application {
    mainClassName = "de.fraunhofer.aisec.analysis.Main"
    // Required to give Ehcache deep reflective access to fields to correctly esitmate the cache size.
    applicationDefaultJvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED", "-Xss10M")
}
tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "6000m"
}

// Persist source files generated by annotation processor to a sane source path so IDEs can use them directly.
sourceSets.configureEach {
    tasks.named<JavaCompile>(compileJavaTaskName) {
        options.annotationProcessorGeneratedSourcesDirectory = file("$projectDir/src/main/generated/annotationProcessor/java/${this@configureEach.name}")
        java.srcDir(file("$projectDir/src/main/generated/annotationProcessor/java/main")) // adding as generated souce dir did not work in Idea 2019.1
    }
}

// Added mark files from dist folder to test resources
sourceSets.getByName("test").resources {
    srcDir("src/dist")
}

tasks.named("compileJava") {
    dependsOn(":spotlessApply")
}


tasks.named("sonarqube") {
    dependsOn(":jacocoTestReport")
}


spotless {
    java {
        targetExclude(
                fileTree(project.projectDir) {
                    include("src/main/generated/**")
                }
        )
        eclipse().configFile(rootProject.file("formatter-settings.xml"))
    }
}

downloadLicenses {
    includeProjectDependencies = true
    dependencyConfiguration = "compileClasspath"
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        // workaround for https://github.com/gradle/gradle/issues/1989
        windowsScript.writeText(windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"))
    }
}
