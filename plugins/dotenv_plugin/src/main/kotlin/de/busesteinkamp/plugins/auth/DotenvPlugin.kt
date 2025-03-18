package de.busesteinkamp.plugins.auth

import de.busesteinkamp.domain.auth.EnvRetriever
import io.github.cdimascio.dotenv.dotenv

class DotenvPlugin : EnvRetriever {

    private val dotenv = dotenv()

    override fun getEnvVariable(key: String): String {
        return dotenv[key];
    }

}