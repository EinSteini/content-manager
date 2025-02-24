package de.busesteinkamp.plugins.server

import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.server.ServerPlugin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class ThreadsServerPlugin : ServerPlugin{
    override fun onLoad(server: Server) {
        server.addRoute(KtorRouteDefinition("auth", HttpMethod.Post, {
            println(call.request)
            call.respond(HttpStatusCode.OK)
        }))
    }

}