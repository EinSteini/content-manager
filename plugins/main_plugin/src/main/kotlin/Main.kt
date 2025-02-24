package de.busesteinkamp

import de.busesteinkamp.adapters.media.MediaFileUploadController
import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.media.UploadMediaFileUseCase
import de.busesteinkamp.plugins.media.InMemoryMediaFileRepository
import de.busesteinkamp.plugins.media.InMemoryPlatformRepository
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.UploadStatus
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import de.busesteinkamp.plugins.media.TxtFile
import de.busesteinkamp.plugins.platform.ThreadsPlatform
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
    val uploadMediaFileUseCase = UploadMediaFileUseCase(mediaFileRepository, platformRepository)
    val getMediaFileUseCase = GetMediaFileUseCase(mediaFileRepository)
    val server: Server = KtorServer(8080);

    // Beispielhafte Verwendung der Use Cases
    val mediaFile: MediaFile = TxtFile(
        filename = "/Users/niklas/DEV/AdvSWEProjekt/content-manager/test.txt", filetype = "text/plain", fileSize = 1234,
        id = UUID.randomUUID(),
        uploadStatus = UploadStatus.INITIAL
    )
    println(mediaFile.toString())

    val threads: Platform = ThreadsPlatform(UUID.randomUUID(), "Threads", server)
    platformRepository.save(threads)
    val mainUser: User = User(UUID.randomUUID(), "main", listOf(threads))
    val publishParameters: PublishParameters = PublishParameters()
    publishParameters.title = "New Post"

    server.start()
    uploadMediaFileUseCase.execute(mediaFile, mainUser, publishParameters)

    delay(5000)

    val uploadedFile = getMediaFileUseCase.execute(mediaFile.id!!)
    println(uploadedFile)

    Scanner(System.`in`).nextLine()
}