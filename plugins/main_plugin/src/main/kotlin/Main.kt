package de.busesteinkamp

import de.busesteinkamp.adapters.media.MediaFileUploadController
import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.media.UploadMediaFileUseCase
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import de.busesteinkamp.domain.media.PlatformRepository
import de.busesteinkamp.plugins.media.JpaMediaFileRepository
import de.busesteinkamp.plugins.media.JpaPlatformRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
open class Main

fun main(args: Array<String>) {
    runApplication<Main>(*args)
    // Hier die Abhängigkeiten manuell erstellen und injizieren
    val mediaFileRepository: MediaFileRepository = JpaMediaFileRepository()
    val platformRepository: PlatformRepository = JpaPlatformRepository()
    val uploadMediaFileUseCase = UploadMediaFileUseCase(mediaFileRepository, platformRepository)
    val getMediaFileUseCase = GetMediaFileUseCase(mediaFileRepository)
    val mediaFileUploadController = MediaFileUploadController(uploadMediaFileUseCase, getMediaFileUseCase)

    // Beispielhafte Verwendung der Use Cases
    val mediaFile = MediaFile(filename = "/Users/niklas/DEV/AdvSWEProjekt/content-manager/test.txt", filetype = "text/plain", fileSize = 1234)
    uploadMediaFileUseCase.execute(mediaFile, listOf("YouTube", "Instagram"))

    val uploadedFile = getMediaFileUseCase.execute(mediaFile.id!!)
    println(uploadedFile)

    // Beende die Anwendung
    exitProcess(0)
}