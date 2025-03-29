package de.busesteinkamp.plugins.auth

import de.busesteinkamp.domain.auth.EnvRetriever
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

class DotenvPlugin : EnvRetriever {

    private var initialized = false
    private var dotenv: Dotenv? = null

    init {
        try {
            dotenv = dotenv()
            initialized = true
        } catch (e: Exception) {
            println("Error loading .env file: ${e.message}")
            initialized = false
        }
    }

    override fun getEnvVariable(key: String): String {
        if (!initialized || dotenv == null) {
            throw IllegalStateException("DotenvPlugin not initialized. Check if .env file is present.")
        }
        return dotenv!![key];
    }

}