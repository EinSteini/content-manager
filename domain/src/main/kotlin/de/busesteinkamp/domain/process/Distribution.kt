package de.busesteinkamp.domain.process

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.PublishParameters
import java.util.*

class Distribution (
    var id: UUID? = null,
    var mediaFile: MediaFile,
    var publishParameters: PublishParameters,
    var uploadStatus: UploadStatus = UploadStatus.INITIAL,
){

}