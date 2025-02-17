plugins {
    kotlin("jvm")
}

group = "de.busesteinkamp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":domain"))
    implementation(project(":plugins:threads_plugin"))
}

tasks.test {
    useJUnitPlatform()
}