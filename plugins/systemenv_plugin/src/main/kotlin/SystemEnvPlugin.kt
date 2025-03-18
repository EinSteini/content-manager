import de.busesteinkamp.domain.auth.EnvRetriever

class SystemEnvPlugin : EnvRetriever {
    override fun getEnvVariable(key: String): String {
        return System.getenv(key) ?: ""
    }
}