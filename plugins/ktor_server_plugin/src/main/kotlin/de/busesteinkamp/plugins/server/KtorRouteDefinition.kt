package de.busesteinkamp.plugins.server

import de.busesteinkamp.adapters.server.DefaultHttpRequest
import de.busesteinkamp.adapters.server.DefaultHttpResponse
import de.busesteinkamp.adapters.server.DefaultRouteDefinition
import de.busesteinkamp.adapters.server.DefaultRoutingContext
import de.busesteinkamp.domain.server.RouteDefinition
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.reflect.*

class KtorRouteDefinition(
    val path: String,
    val method: HttpMethod,
    val handler: suspend RoutingContext.() -> Unit
) : RouteDefinition() {
    companion object{
        fun from(routeDefinition: DefaultRouteDefinition): KtorRouteDefinition {
            return KtorRouteDefinition(
                path = routeDefinition.path,
                method = mapHttpMethod(routeDefinition.method),
                handler = {
                    val queryParameters = call.request.queryParameters
                    val pathVariables = call.request.pathVariables
                    val queryParametersMap = queryParameters.toMap().mapValues { it.value.firstOrNull() ?: "" }
                    val pathVariablesMap = pathVariables.toMap().mapValues { it.value.firstOrNull() ?: "" }

                    val request = DefaultHttpRequest(
                        queryParameters = queryParametersMap,
                        pathVariables = pathVariablesMap
                    )
                    val response = DefaultHttpResponse()

                    val routingContext = DefaultRoutingContext(request, response, routeDefinition)
                    routeDefinition.handler.invoke(routingContext)

                    call.respond(
                        message = mapHttpStatusCode(response.statusCode),
                        typeInfo = TypeInfo(HttpStatusCode::class),
                        // todo: include response body if needed
                    )
                }
            )
        }

        private fun mapHttpMethod(method: de.busesteinkamp.adapters.server.HttpMethod): HttpMethod {
            return when (method) {
                de.busesteinkamp.adapters.server.HttpMethod.GET -> HttpMethod.Get
                de.busesteinkamp.adapters.server.HttpMethod.POST -> HttpMethod.Post
                de.busesteinkamp.adapters.server.HttpMethod.PUT -> HttpMethod.Put
                de.busesteinkamp.adapters.server.HttpMethod.DELETE -> HttpMethod.Delete
                de.busesteinkamp.adapters.server.HttpMethod.HEAD -> HttpMethod.Head
                de.busesteinkamp.adapters.server.HttpMethod.PATCH -> HttpMethod.Patch
                de.busesteinkamp.adapters.server.HttpMethod.OPTIONS -> HttpMethod.Options
            }
        }

        private fun mapHttpStatusCode(code: de.busesteinkamp.adapters.server.HttpStatusCode): HttpStatusCode{
            return when (code){
                de.busesteinkamp.adapters.server.HttpStatusCode.OK -> HttpStatusCode.OK
                de.busesteinkamp.adapters.server.HttpStatusCode.CREATED -> HttpStatusCode.Created
                de.busesteinkamp.adapters.server.HttpStatusCode.ACCEPTED -> HttpStatusCode.Accepted
                de.busesteinkamp.adapters.server.HttpStatusCode.NO_CONTENT -> HttpStatusCode.NoContent
                de.busesteinkamp.adapters.server.HttpStatusCode.BAD_REQUEST -> HttpStatusCode.BadRequest
                de.busesteinkamp.adapters.server.HttpStatusCode.UNAUTHORIZED -> HttpStatusCode.Unauthorized
                de.busesteinkamp.adapters.server.HttpStatusCode.FORBIDDEN -> HttpStatusCode.Forbidden
                de.busesteinkamp.adapters.server.HttpStatusCode.NOT_FOUND -> HttpStatusCode.NotFound
                de.busesteinkamp.adapters.server.HttpStatusCode.METHOD_NOT_ALLOWED -> HttpStatusCode.MethodNotAllowed
                de.busesteinkamp.adapters.server.HttpStatusCode.CONFLICT -> HttpStatusCode.Conflict
                de.busesteinkamp.adapters.server.HttpStatusCode.INTERNAL_SERVER_ERROR -> HttpStatusCode.InternalServerError

            }
        }
    }
}