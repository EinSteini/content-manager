plugins {
    kotlin("jvm") version "2.0.0"
}

group = "de.busesteinkamp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation(project(":adapters"))
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":plugins:inmemory_plugin"))
    implementation(project(":plugins:threads_plugin"))
    implementation(project(":plugins:txt_plugin"))
    implementation(project(":plugins:ktor_server_plugin"))
}

tasks.test {
    useJUnitPlatform()
}