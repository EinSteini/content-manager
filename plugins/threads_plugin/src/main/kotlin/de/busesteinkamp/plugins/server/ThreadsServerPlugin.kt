package de.busesteinkamp.plugins.server

import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.server.ServerPlugin
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import io.ktor.http.*
import io.ktor.server.response.*

class ThreadsServerPlugin(private val platform: ThreadsPlatform, private val path: String) : ServerPlugin {

    override fun onLoad(server: Server) {
        server.addRoute(KtorRouteDefinition(path, HttpMethod.Get) {
            call.respond(HttpStatusCode.OK)
            val authKey = call.request.queryParameters["code"]
            if (authKey != null) {
                platform.receiveAuthKey(authKey)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        })
    }

    override fun onRemove(server: Server) {
        server.removeRoute(path)
    }
}