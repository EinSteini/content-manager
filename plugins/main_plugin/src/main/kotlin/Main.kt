package de.busesteinkamp

import de.busesteinkamp.adapters.media.MediaFileUploadController
import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.media.UploadMediaFileUseCase
import de.busesteinkamp.plugins.media.InMemoryMediaFileRepository
import de.busesteinkamp.de.busesteinkamp.plugins.media.InMemoryPlatformRepository
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import de.busesteinkamp.domain.media.PlatformRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

open class Main

fun main(args: Array<String>): Unit = runBlocking {
    // Hier die Abhängigkeiten manuell erstellen und injizieren
    val mediaFileRepository: MediaFileRepository = InMemoryMediaFileRepository() // Verwende InMemoryMediaFileRepository
    val platformRepository: PlatformRepository = InMemoryPlatformRepository()
    val uploadMediaFileUseCase = UploadMediaFileUseCase(mediaFileRepository, platformRepository)
    val getMediaFileUseCase = GetMediaFileUseCase(mediaFileRepository)
    val mediaFileUploadController = MediaFileUploadController(uploadMediaFileUseCase, getMediaFileUseCase)
    mediaFileUploadController.startServer(8080)

    // Beispielhafte Verwendung der Use Cases
    val mediaFile = MediaFile(filename = "/Users/niklas/DEV/AdvSWEProjekt/content-manager/test.txt", filetype = "text/plain", fileSize = 1234)
    println(mediaFile.toString())
    uploadMediaFileUseCase.execute(mediaFile, listOf("YouTube", "Instagram"))

    delay(5000)

    val uploadedFile = getMediaFileUseCase.execute(mediaFile.id!!)
    println(uploadedFile)
}