package de.busesteinkamp.plugins.platform

import de.busesteinkamp.domain.auth.AuthKey
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.plugins.server.ThreadsLongLivedAccessTokenResponse
import de.busesteinkamp.plugins.server.ThreadsServerPlugin
import de.busesteinkamp.plugins.server.ThreadsShortLivedAccessTokenResponse
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI
import java.util.*

class ThreadsPlatform(id: UUID?, name: String, private val server: Server, private val authKeyRepository: AuthKeyRepository) : Platform(id, name) {

    private var authorized = false

    private val client: HttpClient = HttpClient(CIO){
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val authPath = "/auth"
    private val authAddress = server.getAddress() + authPath

    private val threadsServerPlugin = ThreadsServerPlugin(this, authPath)

    private val clientId: String
    private val clientSecret: String

    private lateinit var apiKey: String

    init {
        val dotenv = dotenv()
        clientId = dotenv["THREADS_APP_CLIENT_ID"]
        clientSecret = dotenv["THREADS_APP_CLIENT_SECRET"]

        val key = authKeyRepository.find(name)

        CoroutineScope(Dispatchers.IO).launch {
            testKey(key)
        }
    }

    override fun upload(mediaFile: MediaFile, publishParameters: PublishParameters) {
        if(!authorized){
            throw IllegalStateException("Platform is not authorized")
        }
        TODO("Not yet implemented")
    }

    private suspend fun testKey(key: AuthKey?){
        if(key != null){
            // todo: check if key is working
            if(key.expiresAt < Date()){
                authKeyRepository.delete(name)
                authorize()
                return
            }
        } else {
            authorize()
            return
        }

        // If key is older than 24 hours, refresh it
        if(key.createdAt.time < Date().time - 1000 * 60 * 60 * 24){
            val refreshedKey = refreshAccessToken(key.key)
            authKeyRepository.update(refreshedKey)
        }

        this.apiKey = key.key
        this.authorized = true
    }

    private fun authorize(){
        this.authorized = false
        server.registerPlugin(threadsServerPlugin)
        val desktop: Desktop = Desktop.getDesktop()
        desktop.browse(URI(
            "https://threads.net/oauth/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=$authAddress" +
                "&scope=threads_basic,threads_content_publish" +
                "&response_type=code"
        ))
    }

    suspend fun receiveAuthKey(key: String) {
        println("Received auth key: $key") // Totally safe and data compliant

        val shortLivedToken = exchangeCodeForShortLivedToken(key)
        val longLivedToken = exchangeShortLivedTokenForLongLivedToken(shortLivedToken)

        authKeyRepository.save(longLivedToken)

        server.unregisterPlugin(threadsServerPlugin)
        this.authorized = true
    }

    private suspend fun exchangeCodeForShortLivedToken(code: String): String {
        val response = client.post("https://graph.threads.net/oauth/access_token") {
            setBody(FormDataContent(Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("code", code)
                append("grant_type", "authorization_code")
                append("redirect_uri", "https://0.0.0.0:8443/auth")
            }))
        }

        val accessTokenResponse: ThreadsShortLivedAccessTokenResponse = response.body()
        println("Received short lived access token: ${accessTokenResponse.access_token}")
        return accessTokenResponse.access_token
    }

    private suspend fun exchangeShortLivedTokenForLongLivedToken(token: String): AuthKey{
        val response = client.get("https://graph.threads.net/access_token"){
            parameter("client_secret", clientSecret)
            parameter("access_token", token)
            parameter("grant_type", "th_exchange_token")
        }


        val accessTokenResponse: ThreadsLongLivedAccessTokenResponse = response.body()
        println("Received long lived access token: ${accessTokenResponse.access_token}")
        val authKey = AuthKey(name, accessTokenResponse.access_token, Date(), Date(Date().time + accessTokenResponse.expires_in * 1000))
        return authKey
    }

    private suspend fun refreshAccessToken(token: String): AuthKey{
        val response = client.get("https://graph.threads.net/refresh_access_token"){
            parameter("access_token", token)
            parameter("grant_type", "th_refresh_token")
        }

        val accessTokenResponse: ThreadsLongLivedAccessTokenResponse = response.body()
        println("Received long lived access token: ${accessTokenResponse.access_token}")
        val authKey = AuthKey(name, accessTokenResponse.access_token, Date(), Date(Date().time + accessTokenResponse.expires_in * 1000))
        return authKey
    }
}