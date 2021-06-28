plugins {
    // built-in
    java
    application
    jacoco
    idea
    `maven-publish`
    `java-library`

    id("org.sonarqube") version "3.3"
    id("com.diffplug.spotless") version "5.14.0"
    id("com.github.hierynomus.license") version "0.16.1"
}


group = "de.fraunhofer.aisec"

/* License plugin needs a special treatment, as long as the main project does not have a license yet.
 * See https://github.com/hierynomus/license-gradle-plugin/issues/155
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

    // Eclipse CDT repo
    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/10.2/cdt-10.2.0/plugins")
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
    implementation("org.slf4j:slf4j-api:1.8.0-beta4") // ok
    api("org.slf4j:log4j-over-slf4j:1.8.0-beta4") // needed for xtext.parser.antlr
    api("org.apache.logging.log4j:log4j-core:2.14.1") // impl in main; used only in test
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")

    // Code Property Graph
    api("de.fraunhofer.aisec:cpg:3.5.1") // ok

    // MARK DSL (use fat jar). changing=true circumvents gradle cache
    //api("de.fraunhofer.aisec.mark:de.fraunhofer.aisec.mark:1.4.0-SNAPSHOT:repackaged") { isChanging = true } // ok
    implementation("com.github.Fraunhofer-AISEC.codyze-mark-eclipse-plugin:de.fraunhofer.aisec.mark:master-SNAPSHOT:repackaged")


    // Pushdown Systems
    api("de.breakpointsec:pushdown:1.1") // ok

    // LSP interface support
    api("org.eclipse.lsp4j:org.eclipse.lsp4j:0.12.0") // ok

    // Interactive console interface support using Jython (Scripting engine)
    implementation("org.python:jython-standalone:2.7.2") // ok

    // Command line interface support
    api("info.picocli:picocli:4.6.1")
    annotationProcessor("info.picocli:picocli-codegen:4.6.1")

    // JSON parser for generation of results file
    implementation("org.json:json:20210307")

    // JsonPath for querying findings description
    implementation("com.jayway.jsonpath:json-path:2.6.0")

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

    // Groovy
    implementation("org.codehaus.groovy:groovy:3.0.7") // fetch a recent groovy otherwise, Java11+ has problems

    // Fast in-memory graph DB (alternative to Neo4J)
    implementation("io.shiftleft:overflowdb-tinkerpop3:0.128")

    // Reflections for OverflowDB and registering Crymlin built-ins
    implementation("org.reflections", "reflections", "0.9.11")

    // Unit tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

application {
    mainClass.set("de.fraunhofer.aisec.analysis.Main")
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
        eclipse().configFile(rootProject.file("formatter-settings.xml"))
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
        windowsScript.writeText(windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"))
    }
}
