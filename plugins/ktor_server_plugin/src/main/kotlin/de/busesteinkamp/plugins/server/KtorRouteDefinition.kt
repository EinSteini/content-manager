package de.busesteinkamp.plugins.server

import de.busesteinkamp.domain.server.RouteDefinition
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.PipelineInterceptor

class KtorRouteDefinition
    (val path: String,
     val method: HttpMethod,
     val handler: suspend RoutingContext.() -> Unit
) :
    RouteDefinition() {
}