package de.busesteinkamp.plugins.server

import de.busesteinkamp.domain.server.RouteDefinition
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.server.ServerPlugin
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.security.KeyStore

class KtorServer(private val port: Int) : Server {
    private val plugins = mutableListOf<ServerPlugin>()
    private val dynamicRoutes = mutableMapOf<String, KtorRouteDefinition>()
    private lateinit var ktorApp: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>
    private var running = false

    override fun start() {
        plugins.forEach { it.onLoad(this) }

        ktorApp = embeddedServer(Netty, applicationEnvironment { log = LoggerFactory.getLogger("ktor.application") },
            { envConfig() }) {
            routing {
                for ((_, routeDef) in dynamicRoutes) {
                    addRouteInternal(routeDef)
                }
            }
        }.start(wait = false)
        running = true

        println("Ktor Server started on port $port")
    }

    override fun stop() {
        if (!running) return
        ktorApp.stop(1000, 2000)
        running = false
        println("Server stopped.")
    }

    override fun registerPlugin(plugin: ServerPlugin) {
        plugins.add(plugin)
    }

    override fun unregisterPlugin(plugin: ServerPlugin) {
        plugin.onRemove(this)
        plugins.remove(plugin)
        restartServer()
    }

    override fun addRoute(route: RouteDefinition) {
        if (route !is KtorRouteDefinition) {
            throw IllegalArgumentException("Route must be of type KtorRouteDefinition")
        }
        println("Adding route: ${route.path}")
        dynamicRoutes[route.path] = route
        restartServer()
    }

    override fun removeRoute(path: String) {
        dynamicRoutes.remove(path)
        restartServer()
    }

    private fun restartServer() {
        if(!running) return
        stop()
        start()
    }

    private fun Routing.addRouteInternal(routeDef: KtorRouteDefinition) {
        when (routeDef.method) {
            HttpMethod.Get -> get(routeDef.path, routeDef.handler)
            HttpMethod.Post -> post(routeDef.path, routeDef.handler)
            HttpMethod.Put -> put(routeDef.path, routeDef.handler)
            HttpMethod.Delete -> delete(routeDef.path, routeDef.handler)
            else -> throw IllegalArgumentException("Unsupported HTTP method")
        }
    }

    private fun ApplicationEngine.Configuration.envConfig() {
        val keyStoreFile = File("ktorServer.jks")
        if (!keyStoreFile.exists()) {
            throw FileNotFoundException("Keystore file not found: ${keyStoreFile.absolutePath}")
        }

        val keyStorePassword = "12345678".toCharArray()
        val privateKeyPassword = "12345678".toCharArray()

        val keyStore = KeyStore.getInstance("JKS").apply {
            load(keyStoreFile.inputStream(), keyStorePassword)
        }

        val sslKeyStore = KeyStore.getInstance("JKS")
        sslKeyStore.load(keyStoreFile.inputStream(), keyStorePassword)

        if (!keyStore.containsAlias("ktorServer")) {
            throw IllegalArgumentException("Alias 'ktorServer' not found in keystore")
        }

        sslConnector(
            keyStore = keyStore,
            keyAlias = "ktorServer",
            keyStorePassword = { keyStorePassword },
            privateKeyPassword = { privateKeyPassword }) {
            port = getPort()
            keyStorePath = keyStoreFile
        }
    }

    override fun getPort(): Int {
        return port
    }

    override fun getAddress(): String {
        return "https://0.0.0.0:$port" // Todo: get actual IP
    }
}
