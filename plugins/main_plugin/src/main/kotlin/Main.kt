package de.busesteinkamp

import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.plugins.media.InMemoryMediaFileRepository
import de.busesteinkamp.plugins.media.InMemoryPlatformRepository
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.process.UploadStatus
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import de.busesteinkamp.plugins.auth.SqliteAuthKeyRepository
import de.busesteinkamp.plugins.media.TxtFile
import de.busesteinkamp.plugins.platform.BlueskyPlatform
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.user.InMemoryUserRepository
import kotlinx.coroutines.delay
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

    val examplePostPath = javaClass.getResource("/example_post.txt")?.path
    if(examplePostPath == null) {
        println("Could not find example_post.txt")
        return@runBlocking
    }
    // Beispielhafte Verwendung der Use Cases
    val mediaFile: MediaFile = TxtFile(
        filename = examplePostPath,
        fileSize = 1234,
        id = UUID.randomUUID()
    )
    println(mediaFile.toString())

    val userId = UUID.randomUUID()
    val threads: Platform = ThreadsPlatform(UUID.randomUUID(), "Threads", server, authKeyRepository)
    val bsky: Platform = BlueskyPlatform(UUID.randomUUID(), "Bluesky")
    val mainUser: User = User(UUID.randomUUID(), "main", listOf(threads, bsky))
    platformRepository.save(threads)
    val publishParameters: PublishParameters = PublishParameters()
    publishParameters.title = "New Post"

    val distribution = Distribution(
        mediaFile = mediaFile,
        publishParameters = publishParameters,
        platforms = mainUser.platforms
    )

    server.start()
    executeDistributionUseCase.execute(distribution)

    Scanner(System.`in`).nextLine()
}