package de.busesteinkamp.plugins.server

import de.busesteinkamp.adapters.server.*
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.server.ServerPlugin
import de.busesteinkamp.plugins.platform.TwitterPlatform

class TwitterServerPlugin(private val platform: TwitterPlatform, private val path: String) : ServerPlugin {

    override fun onLoad(server: Server) {
        server.addRoute(DefaultRouteDefinition(path, HttpMethod.GET, {
            response.statusCode = HttpStatusCode.OK
            val authKey = request.queryParameters["code"]
            if (authKey != null) {
                platform.receiveAuthKey(authKey)
            }else{
                response.statusCode = HttpStatusCode.BAD_REQUEST
            }
        }))
    }

    override fun onRemove(server: Server) {
        server.removeRoute(path)
    }
}