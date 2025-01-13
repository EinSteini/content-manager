plugins {
    java
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.jpa") version "2.0.0"
}

group = "de.busesteinkamp"
version = "0.0.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.hibernate:hibernate-core:6.2.6.Final") // Hibernate Core
    implementation("com.h2database:h2:2.2.224") // H2 Datenbank
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0") // JPA API
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

tasks.test {
    useJUnitPlatform()
}