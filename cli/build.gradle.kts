plugins {
    id("buildlogic.kotlin-application-conventions")
    id("com.gradleup.shadow") version "9.4.1"
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

tasks.named<Jar>("shadowJar") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles an executable fat jar for the CLI"
    destinationDirectory = rootProject.layout.projectDirectory.dir("dist").asFile
    archiveFileName = "krawler.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.assemble {
    dependsOn(tasks.named("shadowJar"))
}
