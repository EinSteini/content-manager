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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.1") // Spring Web MVC

}

tasks.test {
    useJUnitPlatform()
}
