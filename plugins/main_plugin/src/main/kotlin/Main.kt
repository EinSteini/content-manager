package de.busesteinkamp

import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.application.utility.BrowserOpener
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import de.busesteinkamp.plugins.auth.DotenvPlugin
import de.busesteinkamp.plugins.auth.SqliteAuthKeyRepository
import de.busesteinkamp.plugins.media.*
import de.busesteinkamp.plugins.platform.BlueskyPlatform
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import de.busesteinkamp.plugins.platform.TwitterPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.user.InMemoryUserRepository
import de.busesteinkamp.plugins.utility.DesktopBrowserOpener
import kotlinx.coroutines.runBlocking
import java.util.*

open class Main

fun main(args: Array<String>): Unit = runBlocking {
    // Hier die Abhängigkeiten manuell erstellen und injizieren
    val mediaFileRepository: MediaFileRepository = InMemoryMediaFileRepository() // Verwende InMemoryMediaFileRepository
    val platformRepository: PlatformRepository = InMemoryPlatformRepository()
    val userRepository: UserRepository = InMemoryUserRepository()
    val distributionRepository: DistributionRepository = InMemoryDistributionRepository()
    val executeDistributionUseCase = ExecuteDistributionUseCase(distributionRepository)
    val getMediaFileUseCase = GetMediaFileUseCase(mediaFileRepository)
    val server: Server = KtorServer(8443)
    val authKeyRepository: AuthKeyRepository = SqliteAuthKeyRepository()
    val openUrlUseCase = OpenUrlUseCase(true, DesktopBrowserOpener())
    val envRetriever = DotenvPlugin()

    val examplePostPath = javaClass.getResource("/example_post.txt")?.path
    if(examplePostPath == null) {
        println("Could not find example_post.txt")
        return@runBlocking
    }

    val exampleImagePath = javaClass.getResource("/helloworld.jpg")?.path
    if(exampleImagePath == null) {
        println("Could not find helloworld.jpg")
        return@runBlocking
    }

    // Beispielhafte Verwendung der Use Cases
    val mediaFile: MediaFile = TxtFile(
        filename = examplePostPath,
        fileSize = 1234,
        id = UUID.randomUUID()
    )
    println(mediaFile.toString())

    val imageFile: MediaFile = ImageFile(
        filename = exampleImagePath,
        id = UUID.randomUUID(),
        altText = "A nice welcome image"
    )

    val imageFiles: MediaFile = MultipleImageFiles(
        imagePaths = listOf(exampleImagePath, exampleImagePath,exampleImagePath, exampleImagePath ),
        id = UUID.randomUUID(),
        altTexts = listOf("A nice welcome image", "Another nice welcome image","A nice welcome image", "Another nice welcome image" )
    )

    val userId = UUID.randomUUID()
    val threads: Platform = ThreadsPlatform(UUID.randomUUID(), "Threads", server, authKeyRepository, openUrlUseCase, envRetriever)
    val bsky: Platform = BlueskyPlatform(UUID.randomUUID(), "Bluesky", envRetriever)
    val twitter: Platform = TwitterPlatform(UUID.randomUUID(), "Twitter", server, authKeyRepository, openUrlUseCase, envRetriever)
    val mainUser: User = User(UUID.randomUUID(), "main", listOf(twitter))
    platformRepository.save(threads)
    val publishParameters: PublishParameters = PublishParameters()
    publishParameters.title = "Und sogar bis zu 4 Bilder klappen!"

    val distribution = Distribution(
        mediaFile = TxtFile(UUID.randomUUID(), "Tweet with API!"),
        publishParameters = publishParameters,
        platforms = mainUser.platforms
    )

    val imageDistribution = Distribution(
        mediaFile = imageFiles,
        publishParameters = publishParameters,
        platforms = listOf(bsky)
    )

    server.start()
    executeDistributionUseCase.execute(distribution)

    Scanner(System.`in`).nextLine()
}