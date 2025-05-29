package de.busesteinkamp.adapters.server

class DefaultHttpResponse {
    var statusCode: HttpStatusCode = HttpStatusCode.OK
    var body: String? = null
    val headers: MutableMap<String, String> = mutableMapOf()

    fun setHeader(name: String, value: String) {
        headers[name] = value
    }

    fun getHeader(name: String): String? {
        return headers[name]
    }

    fun clearHeaders() {
        headers.clear()
    }
}