plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.module.kotlin)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.maven.artifact)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ktor.client.mock)
}
