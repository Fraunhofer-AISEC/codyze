// A standalone utility project for aggregating code coverage with JaCoCo
// see: https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage_standalone.html
plugins {
    base
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()

    // Eclipse CDT repo --> needed when adding the CPG as an included build
    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/11.3/cdt-11.3.1/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }

    // JitPack -> just-in-time Maven repo assembled from Git repos
    maven {
        setUrl("https://jitpack.io/")
    }
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

val enablePluginSupport: Boolean by rootProject.extra
project.logger.lifecycle("Plugin feature is ${if (enablePluginSupport) "enabled" else "disabled"}")

dependencies {
    jacocoAggregation(projects.codyzeBackends.cpg)
    jacocoAggregation(projects.codyzeCli)
    jacocoAggregation(projects.codyzeCore)
    jacocoAggregation(projects.codyzeSpecificationLanguages.coko.cokoCore)
    jacocoAggregation(projects.codyzeSpecificationLanguages.coko.cokoDsl)

    // Optional and experimental features
    if (enablePluginSupport) jacocoAggregation(project(":codyze-plugins"))
}

tasks.check {
    dependsOn(tasks.named("testCodeCoverageReport"))
}
