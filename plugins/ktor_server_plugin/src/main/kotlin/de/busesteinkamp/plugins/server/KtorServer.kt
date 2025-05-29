package de.busesteinkamp.plugins.server

import de.busesteinkamp.adapters.server.DefaultRouteDefinition
import de.busesteinkamp.domain.server.RouteDefinition
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.server.ServerPlugin
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.io.File

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
        if(running) plugin.onLoad(this)
    }

    override fun unregisterPlugin(plugin: ServerPlugin) {
        plugin.onRemove(this)
        plugins.remove(plugin)
        restartServer()
    }

    override fun addRoute(route: RouteDefinition) {
        if (route !is DefaultRouteDefinition) {
            throw IllegalArgumentException("Only DefaultRouteDefinition is supported")
        }
        val ktorRoute = KtorRouteDefinition.from(route)
        println("Adding route: ${route.path}")
        dynamicRoutes[route.path] = ktorRoute
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
        val keyStorePassword = "12345678"
        val privateKeyPassword = "12345678"

        val keyStoreFile = File("ktorServer.jks")
        val keyStore = buildKeyStore {
            certificate("ktorServer") {
                password = privateKeyPassword
                domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
            }
        }
        keyStore.saveToFile(keyStoreFile, keyStorePassword)

        sslConnector(
            keyStore = keyStore,
            keyAlias = "ktorServer",
            keyStorePassword = { keyStorePassword.toCharArray() },
            privateKeyPassword = { privateKeyPassword.toCharArray() }) {
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

    override fun isRunning(): Boolean {
        return running
    }
}
