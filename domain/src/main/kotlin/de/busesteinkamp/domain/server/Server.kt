package de.busesteinkamp.domain.server

interface Server {
    fun start()
    fun stop()
    fun registerPlugin(plugin: ServerPlugin)
    fun unregisterPlugin(plugin: ServerPlugin)
    fun addRoute(route: RouteDefinition)
    fun removeRoute(path: String)
    fun getPort(): Int
    fun getAddress(): String
}