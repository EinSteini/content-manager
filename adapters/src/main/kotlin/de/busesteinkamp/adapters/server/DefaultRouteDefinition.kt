package de.busesteinkamp.adapters.server

import de.busesteinkamp.domain.server.RouteDefinition

class DefaultRouteDefinition(
    val path: String,
    val method: HttpMethod,
    val handler: suspend DefaultRoutingContext.() -> Unit
): RouteDefinition() {
}