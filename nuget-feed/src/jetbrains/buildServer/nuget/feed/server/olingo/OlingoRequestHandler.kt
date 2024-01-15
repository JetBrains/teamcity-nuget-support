

package jetbrains.buildServer.nuget.feed.server.olingo

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCache
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.nuget.feed.server.olingo.data.OlingoDataSource
import jetbrains.buildServer.nuget.feed.server.olingo.processor.NuGetServiceFactory
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.web.util.WebUtil
import org.apache.olingo.odata2.api.ODataServiceFactory
import org.apache.olingo.odata2.core.servlet.ODataServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Request handler based on Olingo library.
 */
open class OlingoRequestHandler(private val myFeedFactory: NuGetFeedFactory,
                                private val myCache: ResponseCache) : NuGetFeedHandler {
    private val myServletsCache: Cache<String, Pair<ODataServlet, NuGetFeed>>

    init {
        val cacheSize = TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_CACHED_SERVLETS, 32)
        myServletsCache = Caffeine.newBuilder()
            .maximumSize(cacheSize.toLong())
            .executor({ it.run() })
            .build()
    }

    override fun handleRequest(feedData: NuGetFeedData,
                               request: HttpServletRequest,
                               response: HttpServletResponse) {
        if (TeamCityProperties.getBoolean(NuGetFeedConstants.PROP_NUGET_FEED_USE_CACHE)) {
            myCache.getOrCompute(feedData, request, response, { _, _, _ ->
                this.processFeedRequest(feedData, request, response)
            })
        } else {
            processFeedRequest(feedData, request, response)
        }
    }

    private fun processFeedRequest(feedData: NuGetFeedData,
                                   request: HttpServletRequest,
                                   response: HttpServletResponse) {
        LOG.debug("NuGet Feed: " + WebUtil.getRequestDump(request) + "|" + request.requestURI)

        val (servlet, feed) = myServletsCache.get(feedData.key) { _ ->
            ODataServlet().apply {
                this.init(ODataServletConfig(mapOf(
                    ODataServiceFactory.ACCEPT_FORM_ENCODING to "true"
                )))
            } to myFeedFactory.createFeed(feedData)
        } ?: throw Exception("Failed to create servlet")

        val apiVersion = request.getAttribute(NuGetFeedConstants.NUGET_FEED_API_VERSION) as NuGetAPIVersion
        val serviceFactory = NuGetServiceFactory(OlingoDataSource(feed, apiVersion))
        request.setAttribute(ODataServiceFactory.FACTORY_INSTANCE_LABEL, serviceFactory)

        try {
            servlet.service(request, response)
        } catch (e: Throwable) {
            LOG.warnAndDebugDetails("Failed to process request", e)
            throw e
        }
    }

    companion object {
        private val LOG = Logger.getInstance(OlingoRequestHandler::class.java.name)
    }
}
