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
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}