plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
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
    implementation("com.github.scribejava:scribejava-apis:8.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.ktor:ktor-server-core:3.1.0")
    implementation("io.ktor:ktor-server-netty:3.1.0")
    implementation("io.ktor:ktor-server-call-logging:3.1.0")
    implementation("io.ktor:ktor-client-core:3.1.0")
    implementation("io.ktor:ktor-client-cio:3.1.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
}

tasks.test {
    useJUnitPlatform()
}