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

    id("org.sonarqube") version "2.7"
    id("com.diffplug.gradle.spotless") version "3.26.0"
    id("com.github.hierynomus.license") version "0.15.0"
}

group = "de.fraunhofer.aisec"
version = "1.0-SNAPSHOT"

/* License plugin needs a special treatment, as long as the main project does not have a license yet.
   See https://github.com/hierynomus/license-gradle-plugin/issues/155 
*/
gradle.startParameter.excludedTaskNames += "licenseMain"
gradle.startParameter.excludedTaskNames += "licenseTest"


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            val repoUrl = "http://repository.netsec.aisec.fraunhofer.de"

            val releasesRepoUrl = "$repoUrl/repository/releases"
            val snapshotsRepoUrl = "$repoUrl/repository/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = deployUsername
                password = deployPassword
            }
        }
    }
}

repositories {
    mavenLocal()

    mavenCentral()

    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/9.6/cdt-9.6.0/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }

    maven {
        url = uri("http://repository.netsec.aisec.fraunhofer.de/repository/snapshots/")
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

val versions = mapOf(
        "junit5" to "5.3.1",
        "log4j" to "2.11.1",
        "slf4j" to "1.8.0-beta4",
        "lsp4j" to "0.6.0",
        "jersey" to "2.28",
        "javaparser" to "3.11.0",
        "json" to "20190722",
        "commons-lang3" to "3.8.1",
        "jython" to "2.7.1",
        "tinkerpop" to "3.4.3",
        "neo4j-gremlin-bolt" to "0.3.1",
        "xml.bind" to "2.3.1"
)

dependencies {
    api("org.json", "json", versions["json"])

    api("org.apache.commons", "commons-lang3", versions["commons-lang3"])
    api("org.apache.logging.log4j", "log4j-slf4j18-impl", versions["log4j"])
    api("org.apache.logging.log4j", "log4j-core", versions["log4j"])
    // api("org.slf4j", "jul-to-slf4j", versions["slf4j"]) included in cpg as it is needed there
    api("org.slf4j", "log4j-over-slf4j", versions["slf4j"]) // needed for xtext.parser.antlr
    api("org.slf4j", "slf4j-api", versions["slf4j"])

    api("com.github.javaparser", "javaparser-symbol-solver-core", versions["javaparser"])

    // Code Property Graph
    api("de.fraunhofer.aisec", "cpg", "1.1-SNAPSHOT") { setChanging(true) }

    // Ehcache is used to cache heavyweight reflection operations
    api("org.ehcache", "ehcache", "3.8.0")

    // MARK DSL (use fat jar). changing=true circumvents gradle cache
    api("de.fraunhofer.aisec.mark:de.fraunhofer.aisec.mark:1.1.0-SNAPSHOT:repackaged") { setChanging(true) }

    // api stuff
    api("org.glassfish.jersey.inject", "jersey-hk2", versions["jersey"])
    api("org.glassfish.jersey.containers", "jersey-container-grizzly2-http", versions["jersey"])
    api("org.glassfish.jersey.media", "jersey-media-json-jackson", versions["jersey"])

    // LSP
    api("org.eclipse.lsp4j", "org.eclipse.lsp4j", versions["lsp4j"])

    // Gremlin
    api("org.apache.tinkerpop", "gremlin-core", versions["tinkerpop"])
    annotationProcessor("org.apache.tinkerpop", "gremlin-core", versions["tinkerpop"]) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.slf4j", module = "jcl-over-slf4j")
    }      // Newer Gradle versions require specific classpath for annotatation processors
    api("org.apache.tinkerpop", "gremlin-python", versions["tinkerpop"])
    api("org.apache.tinkerpop", "tinkergraph-gremlin", versions["tinkerpop"])
    api("org.apache.tinkerpop", "gremlin-driver", versions["tinkerpop"])
    api("org.apache.tinkerpop", "neo4j-gremlin", versions["tinkerpop"])     // Neo4j multi-label support for gremlin
    api("com.steelbridgelabs.oss", "neo4j-gremlin-bolt", versions["neo4j-gremlin-bolt"])   // For fast bolt:    // access to Neo4J

    // Fast in-memory graph DB (alternative to Neo4J)
    api("io.shiftleft", "overflowdb-tinkerpop3", "0.29")
    api("org.reflections", "reflections", "0.9.11")

    // Pushdown Systems
    api("de.breakpointsec", "pushdown", "1.1")

    // Jython (Scripting engine)
    api("org.python", "jython-standalone", versions["jython"])

    // needed for jersey, not part of JDK anymore
    api("javax.xml.bind", "jaxb-api", versions["xml.bind"])

    testImplementation("org.junit.jupiter", "junit-jupiter-api", versions["junit5"])
    testImplementation("org.junit.jupiter", "junit-jupiter-params", versions["junit5"])
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", versions["junit5"])
}

application {
    mainClassName = "de.fraunhofer.aisec.analysis.Main"
    // Required to give Ehcache deep reflective access to fields to correctly esitmate the cache size.
    applicationDefaultJvmArgs = listOf("--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED")
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
