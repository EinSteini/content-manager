plugins {
    kotlin("jvm")
}

group = "de.busesteinkamp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    testImplementation(kotlin("test"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.1") // Spring Web MVC
    implementation("org.hibernate:hibernate-core:6.2.6.Final")
}

tasks.test {
    useJUnitPlatform()
}