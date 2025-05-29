package de.busesteinkamp.plugins.server

import de.busesteinkamp.adapters.server.DefaultRouteDefinition
import de.busesteinkamp.adapters.server.HttpMethod
import de.busesteinkamp.adapters.server.HttpStatusCode
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.server.ServerPlugin
import de.busesteinkamp.plugins.platform.ThreadsPlatform


class ThreadsServerPlugin(private val platform: ThreadsPlatform, private val path: String) : ServerPlugin {

    override fun onLoad(server: Server) {
        server.addRoute(DefaultRouteDefinition(path, HttpMethod.GET) {
            response.statusCode = HttpStatusCode.OK
            val authKey = request.queryParameters["code"]
            if (authKey != null) {
                platform.receiveAuthKey(authKey)
            } else {
                response.statusCode = HttpStatusCode.BAD_REQUEST
            }
        })
    }

    override fun onRemove(server: Server) {
        server.removeRoute(path)
    }
}