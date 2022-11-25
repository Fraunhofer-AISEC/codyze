plugins {
    kotlin("jvm")
    jacoco
    id("com.diffplug.spotless")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test) // tests are required to run before generating the report
}

// state that JSON schema parser must run before compiling Kotlin
//tasks.named("compileKotlin") {
//    dependsOn("spotlessApply")
//}

//spotless {
//    kotlin {
//        ktfmt().kotlinlangStyle()
//    }
//}