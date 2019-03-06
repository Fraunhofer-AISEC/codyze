plugins {
    // built-in
    java
    application
    antlr
    jacoco
    `maven-publish`
    `java-library`

    id("org.sonarqube") version "2.6"
    id("com.diffplug.gradle.spotless") version "3.18.0"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()

    ivy {
        url = uri("https://download.eclipse.org/tools/cdt/releases/9.6/cdt-9.6.0/plugins")
        patternLayout {
            artifact("/[organisation].[artifact]_[revision].[ext]")
        }
    }

    ivy {
        url = uri("https://ftp.gnome.org/mirror/eclipse.org/oomph/products/repository/plugins/")
        patternLayout {
            artifact("/[organisation].[artifact]_[revision].[ext]")
        }
    }
}

group = "de.fraunhofer.aisec"
version = "1.0-SNAPSHOT"

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
        "jersey" to "2.28",
        "javaparser" to "3.11.0",
        "jython" to "2.7.1b3",
        "tinkerpop" to "3.3.4"
)

dependencies {
    api("org.apache.logging.log4j", "log4j-slf4j18-impl", versions["log4j"])
    api("org.slf4j", "jul-to-slf4j", "1.8.0-beta2")
    implementation("com.github.javaparser", "javaparser-symbol-solver-core", versions["javaparser"])

    implementation("de.fraunhofer.aisec", "cpg", "1.0-SNAPSHOT")

    // api stuff
    //implementation("org.glassfish.hk2", "hk2-core", "2.5.0-b62")
    //implementation("org.glassfish.jersey.core", "jersey-server", versions["jersey"])
    api("org.glassfish.jersey.inject", "jersey-hk2", versions["jersey"])
    //implementation("org.glassfish.jersey.containers", "jersey-container-servlet", versions["jersey"])
    api("org.glassfish.jersey.containers", "jersey-container-grizzly2-http", versions["jersey"])
    api("org.glassfish.jersey.media", "jersey-media-json-jackson", versions["jersey"])

    // seriously eclipse...
    api("org.eclipse", "osgi", "3.13.200.v20181130-2106")
    api("org.eclipse.equinox", "common", "3.10.200.v20181021-1645")
    api("org.eclipse.equinox", "preferences", "3.7.200.v20180827-1235")
    api("org.eclipse.core", "runtime", "3.15.100.v20181107-1343")
    api("org.eclipse.core", "jobs", "3.10.200.v20180912-1356")
    api("org.eclipse.cdt", "core", "6.6.0.201812101042")

    api("org.eclipse.lsp4j", "org.eclipse.lsp4j", "0.6.0")

    api("org.apache.tinkerpop", "gremlin-core", versions["tinkerpop"])
    api("org.apache.tinkerpop", "gremlin-python", versions["tinkerpop"])
    api("org.apache.tinkerpop", "tinkergraph-gremlin", versions["tinkerpop"])
    api("org.apache.tinkerpop", "gremlin-driver", versions["tinkerpop"])
    api("com.steelbridgelabs.oss", "neo4j-gremlin-bolt", "0.3.1")

    api("org.python", "jython-standalone", versions["jython"])


    // needed for jersey, not part of JDK anymore
    api("javax.xml.bind", "jaxb-api", "2.3.1")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", versions["junit5"])
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", versions["junit5"])
}

application {
    mainClassName = "de.fraunhofer.aisec.crymlin.Main"
}
tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks {
    val docker by registering(Exec::class) {
        description = "Builds a docker image based on the Dockerfile."

        dependsOn(build)

        executable = "docker"

        val commit = System.getenv("CI_COMMIT_SHA")

        setArgs(listOf("build",
                "-t", "registry.netsec.aisec.fraunhofer.de/cpg/" + project.name + ':' + (commit?.substring(0, 8)
                ?: "latest"),
                '.'))
    }
}

spotless {
    java {
        targetExclude(
                fileTree(project.projectDir) {
                    include("build/generated-src/**")
                }
        )
        googleJavaFormat()
    }
}
