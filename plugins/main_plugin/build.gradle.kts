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
    implementation(project(":plugins:jpa_plugin"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.1") // Spring Web MVC
}

tasks.test {
    useJUnitPlatform()
}