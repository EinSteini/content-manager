package de.busesteinkamp.domain.server

interface Server {
    fun start()
    fun stop()
    fun registerPlugin(plugin: ServerPlugin)
    fun addRoute(route: RouteDefinition)
    fun removeRoute(path: String)
}