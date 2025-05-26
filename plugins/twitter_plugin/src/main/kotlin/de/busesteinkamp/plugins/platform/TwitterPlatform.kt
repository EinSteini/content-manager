package de.busesteinkamp.plugins.platform

import de.busesteinkamp.adapters.content.TextContent
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKey
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.auth.EnvRetriever
import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.content.ContentType
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.process.UploadStatus
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.plugins.data.LongLivedAccessTokenResponse
import de.busesteinkamp.plugins.data.ShortLivedAccessTokenResponse
import de.busesteinkamp.plugins.data.TwitterApiTweetResponse
import de.busesteinkamp.plugins.server.TwitterServerPlugin
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.*

class TwitterPlatform(
    id: UUID?,
    name: String,
    private val server: Server,
    private val authKeyRepository: AuthKeyRepository,
    private val openUrlUseCase: OpenUrlUseCase,
    private val envRetriever: EnvRetriever
) : SocialMediaPlatform(id, name) {

    private var authorized = false

    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val authPath = "/auth/twitter"
    private val authAddress = server.getAddress() + authPath

    private val codeVerifier = generateCodeVerifier()

    private val twitterServerPlugin = TwitterServerPlugin(this, authPath)

    private val clientId: String
    private val clientSecret: String

    private lateinit var apiKey: String
    private lateinit var refreshToken: String

    private val mediaQueue: Queue<Pair<Content, PublishParameters>> = LinkedList()

    private var uploadInProgress = false

    init {
        clientId = envRetriever.getEnvVariable("X_API_CLIENT_ID")
        clientSecret = envRetriever.getEnvVariable("X_API_CLIENT_SECRET")

        if (clientId == "" || clientSecret == "") {
            throw IllegalStateException("API key or secret not found in .env file")
        }
    }

    override fun upload(content: Content, publishParameters: PublishParameters, callback: ((UploadStatus) -> Unit)?) {
        mediaQueue.add(Pair(content, publishParameters))
        CoroutineScope(Dispatchers.IO).launch {
            testKeyAndReauthorize(key = authKeyRepository.find(name), callback = suspend {
                callback?.invoke(UploadStatus.PENDING)
                handleNewMedia()
            })
            callback?.invoke(UploadStatus.FINISHED)
        }
    }

    private suspend fun handleNewMedia() {
        if (uploadInProgress) {
            return
        }
        if (mediaQueue.isEmpty()) {
            println("No media to upload")
            return
        }

        uploadInProgress = true
        while (mediaQueue.isNotEmpty()) {
            val (content, publishParameters) = mediaQueue.poll()
            when (content.contentType) {
                ContentType.TEXT_PLAIN -> handleTextUpload(content, publishParameters)
                else -> throw IllegalArgumentException("Unsupported media type")
            }
        }
        uploadInProgress = false
    }

    private suspend fun handleTextUpload(content: Content, publishParameters: PublishParameters) {
        val textFile = content as TextContent
        val url = "https://api.twitter.com/2/tweets"

        val response = client.post(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Json)
            }
            setBody("{\"text\": \"${textFile.get()}\"}")
        }

        if (response.status != HttpStatusCode.Created) {
            throw IllegalStateException("Error uploading text file to Twitter. Server responded with status ${response.status}")
        }

        val res: TwitterApiTweetResponse = response.body()
        println("Uploaded text file to Twitter. Tweet ID: ${res.data.id}")
    }

    private suspend fun testKeyAndReauthorize(key: AuthKey?, callback: suspend () -> Unit) {
        if (key != null) {
            // todo: check if key is working
            if (key.expiresAt < Date()) {
                authKeyRepository.delete(name)
                authorize()
                return
            }
        } else {
            authorize()
            return
        }

        // If key is older than 24 hours, refresh it
        if (key.createdAt.time < Date().time - 1000 * 60 * 60 * 24) {
            val refreshedKey = refreshAccessToken(key.key)
            authKeyRepository.update(refreshedKey)
        }

        this.apiKey = key.key.split(":")[0]
        this.authorized = true
        callback()
    }

    private suspend fun authorize() {
        this.authorized = false
        server.registerPlugin(twitterServerPlugin)
        if (!server.isRunning()) {
            server.start()
        }

        val stateString = UUID.randomUUID().toString()
        val codeChallenge = generateCodeChallenge(codeVerifier)

        withContext(Dispatchers.IO) {
            openUrlUseCase.execute(
                "https://twitter.com/i/oauth2/authorize" +
                        "?client_id=$clientId" +
                        "&redirect_uri=$authAddress" +
                        "&scope=tweet.read%20tweet.write%20offline.access%20users.read" +
                        "&response_type=code" +
                        "&state=$stateString" +
                        "&code_challenge=$codeChallenge" +
                        "&code_challenge_method=S256"
            )
        }
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        Random().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    suspend fun receiveAuthKey(authKey: String) {
        println("Received auth key: $authKey") // Totally safe and data compliant

        val shortLivedToken = exchangeCodeForShortLivedToken(authKey)

        authKeyRepository.save(shortLivedToken)
        this.apiKey = shortLivedToken.key.split(":")[0]
        this.authorized = true
        handleNewMedia()
        server.unregisterPlugin(twitterServerPlugin)
    }


    private suspend fun exchangeCodeForShortLivedToken(code: String): AuthKey {
        val credentials = "$clientId:$clientSecret"
        val base64Credentials = Base64.getEncoder().encodeToString(credentials.toByteArray())

        val response = client.post("https://api.twitter.com/2/oauth2/token") {
            headers {
                append(HttpHeaders.Authorization, "Basic $base64Credentials")
            }
            setBody(FormDataContent(Parameters.build {
                append("code", code)
                append("grant_type", "authorization_code")
                append("redirect_uri", authAddress)
                append("code_verifier", codeVerifier)
            }))
        }

        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Error exchanging code for short lived token. Server responded with status ${response.status}")
        }

        val accessTokenResponse: ShortLivedAccessTokenResponse = response.body()
        println("Received short lived access token: ${accessTokenResponse.access_token}")
        return AuthKey(
            name,
            encodeToken(accessTokenResponse.access_token, accessTokenResponse.refresh_token),
            Date(),
            Date(Date().time + accessTokenResponse.expires_in * 1000)
        )
    }

    private suspend fun refreshAccessToken(refreshToken: String): AuthKey {
        val credentials = "$clientId:$clientSecret"
        val base64Credentials = Base64.getEncoder().encodeToString(credentials.toByteArray())

        val response = client.post("https://api.twitter.com/2/oauth2/token") {
            headers {
                append(HttpHeaders.Authorization, "Basic $base64Credentials")
                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.FormUrlEncoded.toString())
            }
            setBody(FormDataContent(Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
            }))
        }

        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Error refreshing access token. Server responded with status ${response.status}")
        }

        val accessTokenResponse: LongLivedAccessTokenResponse = response.body()
        println("Received long lived access token: ${accessTokenResponse.access_token}")
        val authKey = AuthKey(
            name,
            accessTokenResponse.access_token,
            Date(),
            Date(Date().time + accessTokenResponse.expires_in * 1000)
        )
        return authKey
    }

    private fun encodeToken(key: String, refresh: String): String {
        return "$key:$refresh"
    }

    private fun decodeToken(key: String): Pair<String, String> {
        val split = key.split(":")
        return Pair(split[0], split[1])
    }
}