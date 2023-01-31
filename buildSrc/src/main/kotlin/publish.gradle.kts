import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `maven-publish`
}

java {
    //withJavadocJar() // don't use default task because we redefine it relying on Dokka; FIXME maybe there is a better way?
    withSourcesJar()
}

// Configuration of JavaDoc task using Dokka taken from: https://stackoverflow.com/a/66714990
val dokkaHtml by tasks.getting(DokkaTask::class)

val dokkaHtmlJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}
