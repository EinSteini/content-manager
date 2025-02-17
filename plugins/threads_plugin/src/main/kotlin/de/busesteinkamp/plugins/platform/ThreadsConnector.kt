package de.busesteinkamp.plugins.platform

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import java.util.*

class ThreadsConnector(id: UUID?, name: String) : Platform(id, name) {

    override fun upload(mediaFile: MediaFile, publishParameters: PublishParameters) {
        TODO("Not yet implemented")
    }

}