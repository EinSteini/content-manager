package de.busesteinkamp.application.media

import de.busesteinkamp.domain.media.*
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.UploadStatus
import de.busesteinkamp.domain.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadMediaFileUseCase(
    private val mediaFileRepository: MediaFileRepository,
    private val platformRepository: PlatformRepository
) {
    fun execute(mediaFile: MediaFile, user: User, publishParameters: PublishParameters) {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Datei speichern
            val savedMediaFile = mediaFileRepository.save(mediaFile)

            // 2. Plattformen abrufen
            val platforms = user.platforms

            // 3. Datei auf jede Plattform hochladen
            platforms.forEach { platform ->
                try {
                    uploadToPlatform(savedMediaFile, platform)
                    savedMediaFile.uploadStatus = UploadStatus.FINISHED
                } catch (e: Exception) {
                    savedMediaFile.uploadStatus = UploadStatus.FAILED
                    // ... Fehlerbehandlung (Logging, etc.)
                } finally {
                    mediaFileRepository.update(savedMediaFile)
                }
            }
        }
    }

    private fun uploadToPlatform(mediaFile: MediaFile, platform: Platform) {
        // ... Implementierung des Uploads (z.B. mit Plattform-API)
        println("Uploading ${mediaFile.filename} to ${platform.name} ...")
        // Simuliere den Upload (ersetze dies durch die tatsächliche Implementierung)
        Thread.sleep(1000)
        println("Uploaded ${mediaFile.filename} to ${platform.name}")
    }
}