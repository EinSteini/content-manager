package de.busesteinkamp.adapters.media

import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.media.UploadMediaFileUseCase
import de.busesteinkamp.domain.media.MediaFile
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/media")
class MediaFileUploadController(
    private val uploadMediaFileUseCase: UploadMediaFileUseCase,
    private val getMediaFileUseCase: GetMediaFileUseCase
) {

    @PostMapping("/upload")
    fun uploadMediaFile(@RequestBody mediaFileDto: MediaFileDto) {
        val mediaFile = MediaFile(
            filename = mediaFileDto.filename,
            filetype = mediaFileDto.filetype,
            fileSize = mediaFileDto.fileSize
        )
        uploadMediaFileUseCase.execute(mediaFile, mediaFileDto.platformNames)
    }

    @GetMapping("/{id}")
    fun getMediaFile(@PathVariable id: UUID): MediaFileDto? {
        val mediaFile = getMediaFileUseCase.execute(id)
        return mediaFile?.let {
            MediaFileDto(
                filename = mediaFile.filename,
                filetype = mediaFile.filetype,
                fileSize = mediaFile.fileSize,
                platformNames = listOf() // Hier müssten die tatsächlichen Plattformnamen abgerufen werden
            )
        }
    }
}

data class MediaFileDto(
    val filename: String,
    val filetype: String,
    val fileSize: Long,
    val platformNames: List<String>
)