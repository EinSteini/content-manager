plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.busesteinkamp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation(project(":adapters"))
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":plugins:inmemory_plugin"))
    implementation(project(":plugins:threads_plugin"))
    implementation(project(":plugins:bluesky_plugin"))
    implementation(project(":plugins:twitter_plugin"))
    implementation(project(":plugins:ktor_server_plugin"))
    implementation(project(":plugins:sqlite_authkey_storage_plugin"))
    implementation(project(":plugins:generator_gemini_plugin"))
    implementation(project(":plugins:browser_plugin"))
    implementation(project(":plugins:dotenv_plugin"))
    implementation(project(":plugins:systemenv_plugin"))
    implementation(project(":plugins:filereader_plugin"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    manifest {
        attributes(
            "Main-Class" to "de.busesteinkamp.MainKt"
        )
    }
    archiveClassifier.set("")
    mergeServiceFiles()
    from(sourceSets.main.get().output)
}