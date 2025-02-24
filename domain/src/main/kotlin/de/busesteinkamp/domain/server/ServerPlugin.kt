package de.busesteinkamp.domain.server

interface ServerPlugin {
    fun onLoad(server: Server)
}