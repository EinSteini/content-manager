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
    implementation(project(":adapters"))
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":plugins:txt_plugin"))
    implementation(project(":plugins:image_plugin"))
}

tasks.test {
    useJUnitPlatform()
}