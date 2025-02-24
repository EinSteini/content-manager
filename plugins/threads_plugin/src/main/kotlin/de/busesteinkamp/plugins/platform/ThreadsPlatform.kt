package de.busesteinkamp.plugins.platform

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.plugins.server.ThreadsServerPlugin
import java.util.*

class ThreadsPlatform(id: UUID?, name: String, server: Server) : Platform(id, name) {

    val authorized = false

    init {
        server.registerPlugin(ThreadsServerPlugin())
    }

    override fun upload(mediaFile: MediaFile, publishParameters: PublishParameters) {
        TODO("Not yet implemented")
    }

}