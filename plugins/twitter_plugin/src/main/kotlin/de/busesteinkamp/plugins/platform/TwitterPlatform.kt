package de.busesteinkamp.plugins.platform

import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Verb
import com.github.scribejava.core.oauth.OAuth10aService
import de.busesteinkamp.domain.auth.AuthKey
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaType
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.plugins.data.TwitterApiTweetResponse
import de.busesteinkamp.plugins.media.TxtFile
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.*

class TwitterPlatform(id: UUID?, name: String, private val authKeyRepository: AuthKeyRepository) : Platform(id, name) {

    private val client: HttpClient = HttpClient(CIO){
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val apiKey: String
    private val apiSecret: String

    private lateinit var accessToken: OAuth1AccessToken

    private val service: OAuth10aService

    init {
        val dotenv = dotenv()
        apiKey = dotenv["X_API_KEY"]
        apiSecret = dotenv["X_API_KEY_SECRET"]

        service = ServiceBuilder(apiKey)
            .apiSecret(apiSecret)
            .callback("oob")
            .build(TwitterApi.instance())

        val key = authKeyRepository.find(name)
        testKey(key)
    }

    override suspend fun upload(mediaFile: MediaFile, publishParameters: PublishParameters) {
        if(!isDoneInitializing()){
            throw IllegalStateException("Platform is not done initializing")
        }
        when(mediaFile.filetype){
            MediaType.TEXT_PLAIN -> handleTextUpload(mediaFile, publishParameters)
            else -> throw IllegalArgumentException("Unsupported media type")
        }
    }

    override fun isDoneInitializing(): Boolean {
        return this::accessToken.isInitialized
    }

    private fun testKey(key: AuthKey?){
        if (key == null || key.expiresAt.before(Date())) {
            authorize()
            authKeyRepository.save(AuthKey(name, encodeOAuthToken(accessToken), Date(), Date.from(Date().toInstant().plusSeconds(60*60*24*365*10))))
        } else {
            accessToken = decodeOAuthToken(key.key)
        }
    }

    private fun authorize(){
        val requestToken = service.requestToken
        println("Go to this URL and authorize the app: ${service.getAuthorizationUrl(requestToken)}")

        print("Enter the PIN: ")
        val scanner = Scanner(System.`in`)
        val pin = scanner.nextLine()

        val token = service.getAccessToken(requestToken, pin)
        accessToken = token
        println("Access token: $accessToken")
    }

    fun signRequest(url: String, method: Verb, jsonBody: String? = null): String {
        val request = OAuthRequest(method, url)

        // Add required headers
        request.addHeader("Content-Type", "application/json")

        // If it's a POST request, set the JSON body
        if (method == Verb.POST && jsonBody != null) {
            request.setPayload(jsonBody)
        }

        // Sign the request using OAuth1
        service.signRequest(accessToken, request)

        // Return the Authorization header
        return request.getHeaders()["Authorization"] ?: throw Exception("Failed to sign request")
    }

    private suspend fun handleTextUpload(mediaFile: MediaFile, publishParameters: PublishParameters){
        val textFile = mediaFile as TxtFile
        val url = "https://api.twitter.com/2/tweets"

        // Convert to proper JSON string
        val bodyJson = """{"text": "${textFile.textContent}"}"""

        // Sign the request with the JSON body
        val authorization = signRequest(url, Verb.POST, bodyJson)

        val response = client.post(url) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json)
                append(HttpHeaders.Authorization, authorization) // ✅ Correct Authorization header
            }
            setBody(bodyJson) // ✅ Properly formatted JSON body
        }

        println("Response: ${response.status}")
        println("Body: ${response.bodyAsText()}")

        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Error uploading text file to Twitter. Server responded with status ${response.status}")
        }

        val res: TwitterApiTweetResponse = response.body()
        println("Uploaded text file to Twitter. Tweet ID: ${res.data.id}")
    }

    private fun encodeOAuthToken(accessToken: OAuth1AccessToken): String {
        return "${accessToken.token}:${accessToken.tokenSecret}"
    }

    private fun decodeOAuthToken(encodedToken: String): OAuth1AccessToken {
        val parts = encodedToken.split(":")
        return OAuth1AccessToken(parts[0], parts[1])
    }

}