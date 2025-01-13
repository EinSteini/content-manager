pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.21"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "content-manager"
include(":abstraction")
include(":domain")
include(":application")
include(":adapters")
include(":plugins")
include("plugins:jpa_plugin")
include("plugins:json_plugin")
include("plugins:main_plugin")
