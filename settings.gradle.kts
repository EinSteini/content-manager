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
include("plugins:inmemory_plugin")
findProject(":plugins:inmemory_plugin")?.name = "inmemory_plugin"
include("plugins:threads_plugin")
findProject(":plugins:threads_plugin")?.name = "threads_plugin"
include("plugins:txt_plugin")
findProject(":plugins:txt_plugin")?.name = "txt_plugin"
include("plugins:ktor_server_plugin")
findProject(":plugins:ktor_server_plugin")?.name = "ktor_server_plugin"
include("plugins:ktor_server_plugin")
findProject(":plugins:ktor_server_plugin")?.name = "ktor_server_plugin"
include("plugins:sqlite_authkey_storage_plugin")
findProject(":plugins:sqlite_authkey_storage_plugin")?.name = "sqlite_authkey_storage_plugin"
include("plugins:generator_gemini_plugin")
findProject(":plugins:generator_gemini_plugin")?.name = "generator_gemini_plugin"
include("plugins:bluesky_plugin")
findProject(":plugins:bluesky_plugin")?.name = "bluesky_plugin"
include("plugins:image_plugin")
findProject(":plugins:image_plugin")?.name = "image_plugin"
include("plugins:browser_plugin")
findProject(":plugins:browser_plugin")?.name = "browser_plugin"
