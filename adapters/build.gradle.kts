plugins {
    id("java")
    kotlin("jvm") version "2.0.0"
}

group = "de.busesteinkamp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":domain"))
    implementation(project(":application"))
}

tasks.test {
    useJUnitPlatform()
}