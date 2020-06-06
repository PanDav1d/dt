package de.doctag.docsrv

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.features.Compression
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.parseAndSortContentTypeHeader
import io.ktor.response.ApplicationSendPipeline
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteReadChannel

/**
 * Evaluates a route against a content-type in the [HttpHeaders.Accept] header in the request
 * @param contentType is an instance of [ContentType]
 */
data class ExactMatchHttpAcceptRouteSelector(val contentType: ContentType) : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        val headers = context.call.request.headers["Accept"]
        val parsedHeaders = parseAndSortContentTypeHeader(headers)
        if (parsedHeaders.isEmpty())
            return RouteSelectorEvaluation.Missing
        val header = parsedHeaders.filter{
            val ct = ContentType.parse(it.value)
            ct.contentSubtype != "*" && ct.contentType != "*"
        }.firstOrNull { contentType.match(it.value) }
        if (header != null)
            return RouteSelectorEvaluation(true, header.quality)
        return RouteSelectorEvaluation.Failed
    }

    override fun toString(): String = "(contentType:$contentType)"
}

@ContextDsl
fun Route.acceptExcludingWildcards(contentType: ContentType, build: Route.() -> Unit): Route {
    val selector = ExactMatchHttpAcceptRouteSelector(contentType)
    return createChild(selector).apply(build)
}