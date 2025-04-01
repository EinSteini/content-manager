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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.1")
    implementation("org.hibernate:hibernate-core:6.2.6.Final")
    implementation(project(":domain"))
    implementation(project(":application"))
}

tasks.test {
    useJUnitPlatform()
}