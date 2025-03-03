package de.busesteinkamp.plugins.platform

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.plugins.media.TxtFile
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

class BlueskyPlatform(id: UUID?, name: String) : Platform(id, name) {

    private val client: HttpClient = HttpClient(CIO){
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val username: String
    private val password: String

    private var authToken: String = ""

    @Serializable
    data class BlueskyAuthRequest(val identifier: String, val password: String)

    @Serializable
    data class BlueskyAuthResponse(val accessJwt: String, val refreshJwt: String)

    @Serializable
    data class BlueskyPostRecord(val text: String, val createdAt: String)

    @Serializable
    data class BlueskyCreatePostRequest(
        val collection: String,
        val repo: String,
        val record: BlueskyPostRecord
    )

    init {
        val dotenv = dotenv()
        username = dotenv["BSKY_USERNAME"]
        password = dotenv["BSKY_PASSWORD"]
    }

    override fun upload(mediaFile: MediaFile, publishParameters: PublishParameters) {
        if(username == "" || password == ""){
            throw IllegalStateException("Bluesky credentials not set")
        }
        CoroutineScope(Dispatchers.IO).launch {
            authorize()
            when(mediaFile.filetype){
                "text/plain" -> uploadText(mediaFile)
                else -> throw IllegalArgumentException("Unsupported filetype")
            }
        }
    }


    private suspend fun authorize() {
        val response: BlueskyAuthResponse = client.post("https://bsky.social/xrpc/com.atproto.server.createSession") {
            contentType(ContentType.Application.Json)
            setBody(BlueskyAuthRequest(username, password))
        }.body()

        authToken = response.accessJwt
    }

    private suspend fun uploadText(mediaFile: MediaFile){
        println("Uploading text file to Bluesky")
        val textFile = mediaFile as TxtFile
        textFile.loadFile()

        val postRequest = BlueskyCreatePostRequest(
            collection = "app.bsky.feed.post",
            repo = username,
            record = BlueskyPostRecord(textFile.textContent, convertDateToIso8601(Date()))
        )

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken)
            setBody(postRequest)
        }
        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error uploading text file to Threads. Server responded with status ${response.status}")
        }
        println("Text file uploaded to Bluesky")
    }

    private fun convertDateToIso8601(date: Date): String {
        return date.toInstant().toString()
    }

}