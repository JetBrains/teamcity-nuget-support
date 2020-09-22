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

package jetbrains.buildServer.nuget.feed.server.controllers

import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedStdUploadHandler
import jetbrains.buildServer.nuget.feed.server.json.JsonRequestHandler
import jetbrains.buildServer.nuget.feed.server.odata4j.ODataRequestHandler
import jetbrains.buildServer.nuget.feed.server.olingo.OlingoRequestHandler
import jetbrains.buildServer.serverSide.TeamCityProperties
import java.util.*

import javax.servlet.http.HttpServletRequest

/**
 * Provides a concrete NuGet feed handler.
 */
class NuGetFeedProviderImpl(private val myODataRequestHandler: ODataRequestHandler,
                            private val myOlingoRequestHandler: OlingoRequestHandler,
                            private val myJsonRequestHandler: JsonRequestHandler,
                            private val myUploadHandler: NuGetFeedStdUploadHandler) : NuGetFeedProvider {
    private val myHandlers: MutableMap<String, NuGetFeedProvider>

    private val feedHandler: NuGetFeedHandler
        get() = if (TeamCityProperties.getBooleanOrTrue(NuGetFeedConstants.PROP_NUGET_FEED_NEW_SERIALIZER)) {
            myOlingoRequestHandler
        } else {
            myODataRequestHandler
        }

    init {
        myHandlers = TreeMap(String.CASE_INSENSITIVE_ORDER)
        myHandlers["get"] = NuGetFeedProvider { request ->
          if (request.getAttribute(NuGetFeedConstants.NUGET_FEED_API_VERSION) === NuGetAPIVersion.V3) {
            myJsonRequestHandler
          } else {
            feedHandler
          }
        }
        myHandlers["put"] = NuGetFeedProvider { request ->
            if ("/" == request.pathInfo) {
                myUploadHandler
            } else {
                null
            }
        }
        myHandlers["post"] = NuGetFeedProvider{ request ->
            if (request.pathInfo.startsWith("/\$batch")) {
                feedHandler
            } else {
                null
            }
        }
    }

    override fun getHandler(request: HttpServletRequest): NuGetFeedHandler? {
        val provider = myHandlers[request.method] ?: return null
        return provider.getHandler(request)
    }
}
