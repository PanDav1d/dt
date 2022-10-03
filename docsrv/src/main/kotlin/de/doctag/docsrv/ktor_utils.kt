package de.doctag.docsrv


import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.parseAndSortContentTypeHeader
import io.ktor.routing.*
import io.ktor.util.pipeline.ContextDsl
import kweb.plugins.KwebPlugin
import org.jsoup.nodes.Document

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

class NoZoomPlugin: KwebPlugin() {
    override fun decorate(doc: Document) {
        super.decorate(doc)


        val meta = doc.head().allElements.find { it.normalName() == "meta" && it.attr("name") == "viewport"}
        meta?.attr("content", "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no")

        doc.head().appendElement("meta")
            .attr("id", "K_head_ios")
            .attr("name", "apple-mobile-web-app-capable")
            .attr("content", "yes")

        doc.head().appendElement("meta")
            .attr("name", "Description")
            .attr("content", "Doctag")

        doc.head().appendElement("link")
            .attr("rel", "stylesheet")
            .attr("type", "text/css")
            .attr("href", "/ressources/override.css")

        doc.head().appendElement("style")
            .attr("type", "text/css")
            .text("""
                
            """.trimIndent())
    }
}