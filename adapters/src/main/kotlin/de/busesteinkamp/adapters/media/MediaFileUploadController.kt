package de.busesteinkamp.adapters.media

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import de.busesteinkamp.application.media.GetMediaFileUseCase
import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.net.InetSocketAddress
import java.util.UUID

class MediaFileUploadController(
    private val executeDistributionUseCase: ExecuteDistributionUseCase,
    private val getMediaFileUseCase: GetMediaFileUseCase
) : HttpHandler {

    private val json = Json { prettyPrint = true }

    override fun handle(exchange: HttpExchange) {
        when (exchange.requestMethod) {
            "POST" -> {
                if (exchange.requestURI.path == "/media/upload") {
                    handleUpload(exchange)
                }
            }
            "GET" -> {
                if (exchange.requestURI.path.startsWith("/media/")) {
                    handleGet(exchange)
                }
            }
            else -> {
                // Nicht unterstützte HTTP-Methode
                exchange.sendResponseHeaders(405, -1)
            }
        }
        exchange.close()
    }

    private fun handleUpload(exchange: HttpExchange) {
//        try {
//            val reader = BufferedReader(InputStreamReader(exchange.requestBody))
//            val jsonText = reader.readText()
//            val mediaFileDto = json.decodeFromString<MediaFileDto>(jsonText)
//            val mediaFile = MediaFile(
//                filename = mediaFileDto.filename,
//                filetype = mediaFileDto.filetype,
//                fileSize = mediaFileDto.fileSize
//            )
//            uploadMediaFileUseCase.execute(mediaFile, mediaFileDto.platformNames)
//            exchange.sendResponseHeaders(200, -1)
//        } catch (e: Exception) {
//            // Fehlerbehandlung
//            exchange.sendResponseHeaders(500, -1)
//            val output: OutputStream = exchange.responseBody
//            val response = e.message ?: "Interner Serverfehler"
//            output.write(response.toByteArray())
//            output.flush()
//        }
    }

    private fun handleGet(exchange: HttpExchange) {
        try {
            val path = exchange.requestURI.path
            val id = UUID.fromString(path.substringAfterLast("/"))
            val mediaFile = getMediaFileUseCase.execute(id)
            if (mediaFile != null) {
                val response = json.encodeToString(mediaFile)
                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                val output = exchange.responseBody
                output.write(response.toByteArray())
                output.flush()
            } else {
                exchange.sendResponseHeaders(404, -1)
                val output: OutputStream = exchange.responseBody
                val response = "Datei nicht gefunden"
                output.write(response.toByteArray())
                output.flush()
            }
        } catch (e: Exception) {
            // Fehlerbehandlung
            exchange.sendResponseHeaders(500, -1)
            val output: OutputStream = exchange.responseBody
            val response = e.message ?: "Interner Serverfehler"
            output.write(response.toByteArray())
            output.flush()
        }
    }

    fun startServer(port: Int) {
        val server = HttpServer.create(InetSocketAddress(port), 0)
        server.createContext("/media", this)
        server.executor = null // Verwenden Sie den Standard-Executor
        server.start()
        println("Server gestartet auf Port $port")
    }
}

data class MediaFileDto(
    val filename: String,
    val filetype: String,
    val fileSize: Long,
    val platformNames: List<String>
)