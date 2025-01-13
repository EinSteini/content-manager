package de.busesteinkamp.application.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import de.busesteinkamp.domain.media.Platform
import de.busesteinkamp.domain.media.PlatformRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class UploadMediaFileUseCase(
    private val mediaFileRepository: MediaFileRepository,
    private val platformRepository: PlatformRepository
) {
    fun execute(mediaFile: MediaFile, platformNames: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Datei speichern
            val savedMediaFile = mediaFileRepository.save(mediaFile)

            // 2. Plattformen abrufen
            val platforms = platformNames.mapNotNull { platformRepository.findByName(it) }

            // 3. Datei auf jede Plattform hochladen
            platforms.forEach { platform ->
                try {
                    uploadToPlatform(savedMediaFile, platform)
                    savedMediaFile.uploadStatus = MediaFile.UploadStatus.UPLOADED
                } catch (e: Exception) {
                    savedMediaFile.uploadStatus = MediaFile.UploadStatus.FAILED
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