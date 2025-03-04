package de.busesteinkamp.plugins.platform

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.plugins.media.ImageFile
import de.busesteinkamp.plugins.media.MultipleImageFiles
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
import io.ktor.http.content.*
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
    private data class BlueskyAuthRequest(val identifier: String, val password: String)

    @Serializable
    private data class BlueskyAuthResponse(val accessJwt: String, val refreshJwt: String)

    @Serializable
    private data class BlueskyPostRecord(val text: String, val createdAt: String, val embed: BlueskyPostEmbedImages? = null, val `$type`: String = "app.bsky.feed.post")

    @Serializable
    private data class BlueskyCreatePostRequest(
        val collection: String,
        val repo: String,
        val record: BlueskyPostRecord
    )

    @Serializable
    private data class BlueskyUploadResponse(val blob: BlueskyBlob)

    @Serializable
    private data class BlueskyBlob(val ref: BlueskyBlobRef, val mimeType: String, val `$type`: String, val size: Int)

    @Serializable
    private data class BlueskyBlobRef(val `$link`: String)

    @Serializable
    private data class BlueskyPostEmbedImages(
        val images: List<BlueskyBlobImage>,
        val `$type`: String
    )

    @Serializable
    private data class BlueskyBlobImage(val alt: String, val image: BlueskyBlob) // todo: aspect ratio missing

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
                "text/plain" -> handleTextPost(mediaFile)
                "image/jpeg"-> handleImagePost(mediaFile, publishParameters)
                "image/png" -> handleImagePost(mediaFile, publishParameters)
                "image/multiple" -> handleMultipleImagePost(mediaFile, publishParameters)
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

    private suspend fun handleTextPost(mediaFile: MediaFile){
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

    private suspend fun handleImagePost(mediaFile: MediaFile, publishParameters: PublishParameters){
        val blobRef = uploadImage(mediaFile)
        val blobRefs = listOf(BlueskyBlobImage(
            alt = (mediaFile as ImageFile).altText,
            image = blobRef.blob
        ))
        createPostWithImages(blobRefs, publishParameters)
    }

    private suspend fun handleMultipleImagePost(mediaFile: MediaFile, publishParameters: PublishParameters){
        val multipleImageFile = mediaFile as MultipleImageFiles

        if(multipleImageFile.imageFiles.isEmpty()){
            throw IllegalArgumentException("No image files provided")
        }

        if(multipleImageFile.imageFiles.size > 4){
            throw IllegalArgumentException("Maximum of 4 images allowed")
        }

        val blobs = mutableListOf<BlueskyBlobImage>()
        multipleImageFile.imageFiles.forEach({
            val blob = uploadImage(it).blob
            blobs.add(
                BlueskyBlobImage(
                    alt = it.altText,
                    image = blob
                )
            )
        })

        createPostWithImages(blobs, publishParameters)
     }

    private suspend fun uploadImage(mediaFile: MediaFile): BlueskyUploadResponse {
        println("Uploading image file to Bluesky")
        val imageFile = mediaFile as ImageFile

        if(imageFile.fileSize > 1000000){
            throw IllegalArgumentException("Image file too large. Maximum size is 1MB")
        }

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.uploadBlob") {
            bearerAuth(authToken)
            setBody(ByteArrayContent(imageFile.fileContent, contentType = ContentType.parse(imageFile.filetype)))
        }

        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error uploading image file to Threads. Server responded with status ${response.status}")
        }
        println("Image file uploaded to Bluesky")

        return response.body()
    }

    private suspend fun createPostWithImages(blobs: List<BlueskyBlobImage>, publishParameters: PublishParameters) {
        val postRequest = BlueskyCreatePostRequest(
            repo = username,
            record = BlueskyPostRecord(
                `$type` = "app.bsky.feed.post",
                text = publishParameters.title,
                createdAt = convertDateToIso8601(Date()),
                embed = BlueskyPostEmbedImages(
                    images = blobs,
                    `$type` = "app.bsky.embed.images"
                )
            ),
            collection = "app.bsky.feed.post"
        )

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            bearerAuth(authToken)
            contentType(ContentType.Application.Json)
            setBody(postRequest)
        }

        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error creating post with images on Threads. Server responded with status ${response.status}: ${response.bodyAsText()}")
        }
    }

    private fun convertDateToIso8601(date: Date): String {
        return date.toInstant().toString()
    }

}