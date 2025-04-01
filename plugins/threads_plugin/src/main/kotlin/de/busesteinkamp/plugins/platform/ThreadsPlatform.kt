package de.busesteinkamp.plugins.platform

import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKey
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.auth.EnvRetriever
import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.content.ContentType
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.adapters.content.TxtContent
import de.busesteinkamp.plugins.server.ThreadsServerPlugin
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

class ThreadsPlatform(id: UUID?, name: String, private val server: Server, private val authKeyRepository: AuthKeyRepository, private val openUrlUseCase: OpenUrlUseCase, private val envRetriever: EnvRetriever) : Platform(id, name) {

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
        clientId = envRetriever.getEnvVariable("THREADS_APP_CLIENT_ID")
        clientSecret = envRetriever.getEnvVariable("THREADS_APP_CLIENT_SECRET")
    }

    @Serializable
    private data class MediaContainerResponse(val id: String)

    @Serializable
    data class LongLivedAccessTokenResponse(val access_token: String, val token_type: String, val expires_in: Int)

    @Serializable
    data class ShortLivedAccessTokenResponse(val access_token: String, val user_id: String)

    override suspend fun upload(content: Content, publishParameters: PublishParameters) {
        testKey(key = authKeyRepository.find(name))
        if(!authorized || apiKey == ""){
            throw IllegalStateException("Platform is not authorized")
        }
        when(content.contentType){
            ContentType.TEXT_PLAIN -> uploadText(content)
            else -> {
                throw IllegalArgumentException("Unsupported media type")
            }
        }
    }

    private suspend fun uploadText(content: Content){
        println("Uploading text file to Threads")
        val textFile = content as TxtContent

        var response = client.post("https://graph.threads.net/v1.0/me/threads"){
            parameter("media_type", "TEXT")
            parameter("text", textFile.get())
            parameter("access_token", apiKey)
        }
        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error uploading text file to Threads. Server responded with status ${response.status}")
        }
        val mediaContainerResponse: MediaContainerResponse = response.body()

        println("Uploaded text file to Threads. Media container ID: ${mediaContainerResponse.id}")

        // delay for 30 seconds to give Threads time to process the media
        // (documentation says it can take up to 30 seconds)
        delay(30 * 1000)

        response = client.post("https://graph.threads.net/v1.0/me/threads_publish"){
            parameter("creation_id", mediaContainerResponse.id)
            parameter("access_token", apiKey)
        }
        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error publishing text file to Threads. Server responded with status ${response.status}")
        }
        println("Published text file to Threads")
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

    private suspend fun authorize(){
        this.authorized = false
        server.registerPlugin(threadsServerPlugin)
        withContext(Dispatchers.IO) {
            openUrlUseCase.execute(
                    "https://threads.net/oauth/authorize" +
                            "?client_id=$clientId" +
                            "&redirect_uri=$authAddress" +
                            "&scope=threads_basic,threads_content_publish" +
                            "&response_type=code"
                )
        }
    }

    suspend fun receiveAuthKey(key: String) {
        println("Received auth key: $key") // Totally safe and data compliant

        val shortLivedToken = exchangeCodeForShortLivedToken(key)
        val longLivedToken = exchangeShortLivedTokenForLongLivedToken(shortLivedToken)

        authKeyRepository.save(longLivedToken)
        this.apiKey = longLivedToken.key
        this.authorized = true
        server.unregisterPlugin(threadsServerPlugin)
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

        val accessTokenResponse: ShortLivedAccessTokenResponse = response.body()
        println("Received short lived access token: ${accessTokenResponse.access_token}")
        return accessTokenResponse.access_token
    }

    private suspend fun exchangeShortLivedTokenForLongLivedToken(token: String): AuthKey{
        val response = client.get("https://graph.threads.net/access_token"){
            parameter("client_secret", clientSecret)
            parameter("access_token", token)
            parameter("grant_type", "th_exchange_token")
        }


        val accessTokenResponse: LongLivedAccessTokenResponse = response.body()
        println("Received long lived access token: ${accessTokenResponse.access_token}")
        val authKey = AuthKey(name, accessTokenResponse.access_token, Date(), Date(Date().time + accessTokenResponse.expires_in * 1000))
        return authKey
    }

    private suspend fun refreshAccessToken(token: String): AuthKey{
        val response = client.get("https://graph.threads.net/refresh_access_token"){
            parameter("access_token", token)
            parameter("grant_type", "th_refresh_token")
        }

        val accessTokenResponse: LongLivedAccessTokenResponse = response.body()
        println("Received long lived access token: ${accessTokenResponse.access_token}")
        val authKey = AuthKey(name, accessTokenResponse.access_token, Date(), Date(Date().time + accessTokenResponse.expires_in * 1000))
        return authKey
    }
}