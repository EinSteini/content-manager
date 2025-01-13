package de.busesteinkamp.domain.media

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.UUID

@Entity
class MediaFile(
    @Id
    @GeneratedValue
    var id: UUID? = UUID.randomUUID(),
    var filename: String,
    var filetype: String,
    var fileSize: Long,
    var uploadStatus: UploadStatus = UploadStatus.PENDING
) {
    constructor() : this(null, "", "", 0, UploadStatus.PENDING)

    enum class UploadStatus {
        PENDING, UPLOADED, FAILED
    }
}