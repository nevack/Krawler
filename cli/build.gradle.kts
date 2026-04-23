import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar

plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.clikt)
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    runtimeOnly(libs.slf4j.nop)
}

application {
    mainClass = "dev.nevack.krawler.cli.MainKt"
}

val fatJar by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles an executable fat jar for the CLI"
    archiveClassifier = "all"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}

tasks.assemble {
    dependsOn(fatJar)
}
