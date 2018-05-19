/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.feed.server.olingo

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.diagnostic.Logger
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
import org.apache.olingo.odata2.core.servlet.ODataServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Request handler based on Olingo library.
 */
class OlingoRequestHandler(private val myFeedFactory: NuGetFeedFactory,
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

        val (servlet, feed) = myServletsCache.get(feedData.name) { _ ->
            ODataServlet().apply {
                this.init(ODataServletConfig())
            } to myFeedFactory.createFeed(feedData)
        } ?: throw Exception("Failed to create servlet")

        val serviceFactory = NuGetServiceFactory(OlingoDataSource(feed))
        request.setAttribute("org.apache.olingo.odata2.service.factory.instance", serviceFactory)

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
