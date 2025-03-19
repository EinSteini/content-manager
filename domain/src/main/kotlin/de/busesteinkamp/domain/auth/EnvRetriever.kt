package de.busesteinkamp.domain.auth

interface EnvRetriever {
    fun getEnvVariable(key: String): String
}