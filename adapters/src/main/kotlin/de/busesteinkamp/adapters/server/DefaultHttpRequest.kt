package de.busesteinkamp.adapters.server

class DefaultHttpRequest(
    val queryParameters: Map<String, String> = emptyMap(),
    val pathVariables: Map<String, String> = emptyMap()
) {

}