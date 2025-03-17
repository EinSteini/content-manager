package de.busesteinkamp.plugins.platform

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaType
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.plugins.data.*
import de.busesteinkamp.plugins.media.ImageFile
import de.busesteinkamp.plugins.media.MultipleImageFiles
import de.busesteinkamp.plugins.media.TxtFile
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
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

    init {
        val dotenv = dotenv()
        username = dotenv["BSKY_USERNAME"]
        password = dotenv["BSKY_PASSWORD"]
    }

    override suspend fun upload(mediaFile: MediaFile, publishParameters: PublishParameters) {
        if(username == "" || password == ""){
            throw IllegalStateException("Bluesky credentials not set")
        }
        authorize()
        when(mediaFile.filetype){
            MediaType.TEXT_PLAIN -> handleTextPost(mediaFile)
            MediaType.IMAGE_JPEG-> handleImagePost(mediaFile, publishParameters)
            MediaType.IMAGE_PNG -> handleImagePost(mediaFile, publishParameters)
            MediaType.IMAGE_MULTIPLE -> handleMultipleImagePost(mediaFile, publishParameters)
            else -> throw IllegalArgumentException("Unsupported filetype")
        }
    }

    override fun isDoneInitializing(): Boolean {
        return true
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
            record = BlueskyPostRecord(textFile.textContent, convertDateToIso8601(Date()), facets = parseFacets(textFile.textContent)),
        )

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken)
            setBody(postRequest)
        }
        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error uploading text file to Bluesky. Server responded with status ${response.status}: ${response.bodyAsText()}")
        }
        println("Text file uploaded to Bluesky")
    }

    private suspend fun handleImagePost(mediaFile: MediaFile, publishParameters: PublishParameters){
        val blobRef = uploadImage(mediaFile)
        val blobRefs = listOf(
            BlueskyBlobImage(
            alt = (mediaFile as ImageFile).altText,
            image = blobRef.blob
        )
        )
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
            setBody(ByteArrayContent(imageFile.fileContent, contentType = ContentType.parse(imageFile.filetype.text)))
        }

        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error uploading image file to Bluesky. Server responded with status ${response.status}: ${response.bodyAsText()}")
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
                ),
                facets = parseFacets(publishParameters.title)
            ),
            collection = "app.bsky.feed.post"
        )

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            bearerAuth(authToken)
            contentType(ContentType.Application.Json)
            setBody(postRequest)
        }

        if(response.status != HttpStatusCode.OK){
            throw IllegalStateException("Error creating post with images on Bluesky. Server responded with status ${response.status}: ${response.bodyAsText()}")
        }
    }

    private fun convertDateToIso8601(date: Date): String {
        return date.toInstant().toString()
    }

    private suspend fun parseFacets(text: String): List<BlueskyFacet> {
        val mentions = parseMentions(text)
        val urls = parseUrls(text)
        val hashtags = parseHashtags(text)

        val facets = mutableListOf<BlueskyFacet>()

        mentions.forEach {
            val response = client.get("https://bsky.social/xrpc/com.atproto.identity.resolveHandle"){
                parameter("handle", it.handle)
            }

            if (response.status == HttpStatusCode.OK) {
                val did: BlueskyHandleResolve = response.body()
                facets.add(
                    BlueskyFacet(
                        index = BlueskyFacetIndex(it.start, it.end),
                        features = listOf(
                            BlueskyFacetFeature(
                                `$type` = "app.bsky.richtext.facet#mention",
                                did = did.did
                            )
                        )
                    )
                )
            }
        }

        urls.forEach {
            facets.add(
                BlueskyFacet(
                    index = BlueskyFacetIndex(it.start, it.end),
                    features = listOf(BlueskyFacetFeature(`$type` = "app.bsky.richtext.facet#link", uri = it.url))
                )
            )
        }

        hashtags.forEach {
            facets.add(
                BlueskyFacet(
                    index = BlueskyFacetIndex(it.start, it.end),
                    features = listOf(BlueskyFacetFeature(`$type` = "app.bsky.richtext.facet#tag", tag = it.hashtag))
                )
            )
        }

        return facets
    }

    private fun parseMentions(text: String): List<MentionSpan> {
        val spans = mutableListOf<MentionSpan>()

        // Regex based on AT Protocol handle syntax
        val mentionRegex = """(?:\W|^)(@([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)""".toRegex()

        mentionRegex.findAll(text).forEach { matchResult ->
            val handle = matchResult.groups[1]?.value?.substring(1) ?: return@forEach
            spans.add(MentionSpan(matchResult.range.first, matchResult.range.last + 1, handle))
        }
        return spans
    }

    private fun parseUrls(text: String): List<UrlSpan> {
        val spans = mutableListOf<UrlSpan>()

        // Regex for detecting URLs
        val urlRegex = """(?:\W|^)(https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*[-a-zA-Z0-9@%_+~#//=])?)""".toRegex()

        urlRegex.findAll(text).forEach { matchResult ->
            val url = matchResult.groups[1]?.value ?: return@forEach
            spans.add(UrlSpan(matchResult.range.first, matchResult.range.last + 1, url))
        }
        return spans
    }

    private fun parseHashtags(text: String): List<HashtagSpan> {
        val spans = mutableListOf<HashtagSpan>()

        // Regex for detecting hashtags
        val hashtagRegex = """(?:\W|^)(#[a-zA-Z0-9_]+)""".toRegex()

        hashtagRegex.findAll(text).forEach { matchResult ->
            val hashtag = matchResult.groups[1]?.value ?: return@forEach
            spans.add(HashtagSpan(matchResult.range.first, matchResult.range.last + 1, hashtag))
        }
        return spans
    }

}