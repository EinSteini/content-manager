package de.busesteinkamp.adapters.server

class DefaultRoutingContext(
    val request: DefaultHttpRequest,
    val response: DefaultHttpResponse,
    val definition: DefaultRouteDefinition
) {

}