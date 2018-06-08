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

package jetbrains.buildServer.nuget.feed.server.odata4j

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.diagnostic.Logger
import com.sun.jersey.spi.container.servlet.ServletContainer
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCache
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.util.Util
import jetbrains.buildServer.web.util.WebUtil
import org.odata4j.stax2.XMLFactoryProvider2
import org.odata4j.stax2.XMLWriterFactory2
import org.odata4j.stax2.domimpl.DomXMLFactoryProvider2
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:49
 */
open class ODataRequestHandler(private val myFeedFactory: NuGetFeedFactory,
                               private val myCache: ResponseCache) : NuGetFeedHandler {

    private val myServletsCache: Cache<String, ServletContainer>

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
        XMLFactoryProvider2.setInstance(DOM_XML_FACTORY_PROVIDER_2)
        LOG.debug("NuGet Feed: " + WebUtil.getRequestDump(request) + "|" + request.requestURI)

        val apiVersion = request.getAttribute(NuGetFeedConstants.NUGET_FEED_API_VERSION) as NuGetAPIVersion
        val servletContainer = myServletsCache.get(feedData.key, {
            Util.doUnderContextClassLoader<ServletContainer, ServletException>(javaClass.classLoader) {
                val feed = myFeedFactory.createFeed(feedData)
                ServletContainer(NuGetODataApplication(NuGetProducerHolder(feed, apiVersion))).apply {
                    this.init(ODataServletConfig())
                }
            }
        })

        if (servletContainer == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create servlet container")
            return
        }

        Util.doUnderContextClassLoader<Any, Exception>(javaClass.classLoader) {
            servletContainer.service(request, response)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ODataRequestHandler::class.java.name)
        private val DOM_XML_FACTORY_PROVIDER_2 = object : DomXMLFactoryProvider2() {
            override fun newXMLWriterFactory2() = XMLWriterFactory2 { ManualXMLWriter3(it) }
        }
    }
}
