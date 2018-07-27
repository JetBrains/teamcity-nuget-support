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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RecentNuGetRequests
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RequestWrapper
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.WebControllerManager
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.HttpMethod

/**
 * Entry point for nuget feed.
 */
class NuGetFeedController(web: WebControllerManager,
                          private val mySettings: NuGetServerSettings,
                          private val myRequestsList: RecentNuGetRequests,
                          private val myFeedProvider: NuGetFeedProvider,
                          private val myProjectManager: ProjectManager,
                          private val myRepositoryManager: RepositoryManager)
    : BaseController() {

    init {
        setSupportedMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
        web.registerController(NuGetServerSettings.DEFAULT_PATH + "/**", this)
        web.registerController(NuGetServerSettings.PROJECT_PATH + "/**", this)
    }

    override fun doHandle(request: HttpServletRequest,
                          response: HttpServletResponse): ModelAndView? {
        if (!mySettings.isNuGetServerEnabled) {
            return NuGetResponseUtil.nugetFeedIsDisabled(response)
        }

        val (feedPath, projectId, feedId, apiVersion) = getPathComponents(request)
        if (projectId.isEmpty() || feedId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid request to NuGet Feed")
            return null
        }

        val project = myProjectManager.findProjectByExternalId(projectId)
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "NuGet Feed project $projectId not found")
            return null
        }

        if (!myRepositoryManager.hasRepository(project, PackageConstants.NUGET_PROVIDER_ID, feedId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "NuGet Feed $feedId not found")
            return null
        }

        // Set NuGet feed API version
        request.setAttribute(NuGetFeedConstants.NUGET_FEED_API_VERSION, NuGetAPIVersion.valueOf(apiVersion))
        val requestWrapper = object : RequestWrapper(request, feedPath) {
            override fun getQueryString(): String? {
                val queryString = super.getQueryString()?.let {
                    SEM_VER_LEVEL.replaceFirst(it, "$1'$2'$3")
                }
                return if (queryString == null || !super.getRequestURI().endsWith("FindPackagesById()")) {
                    queryString
                } else {
                    // NuGet client in VS 2015 Update 2 introduced breaking change where
                    // instead of `id` parameter passed `Id` while OData is case sensitive
                    QUERY_ID.replaceFirst(queryString, "id=$2")
                }
            }
        }

        var requestPath = WebUtil.getPathWithoutAuthenticationType(requestWrapper)
        if (!requestPath.startsWith("/")) requestPath = "/$requestPath"

        val path = requestPath.substring(feedPath.length)
        val query = requestWrapper.queryString
        val pathAndQuery = "${requestWrapper.method} $path" + if (query != null) "?$query" else ""

        val feedHandler = myFeedProvider.getHandler(requestWrapper)
        if (feedHandler == null) {
            LOG.debug(String.format("%s: %s", UNSUPPORTED_REQUEST, pathAndQuery))
            // error response according to OData spec for unsupported operations (modification operations)
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, UNSUPPORTED_REQUEST)
            return null
        }

        val feedData = NuGetFeedData(project.projectId, feedId)
        val startTime = Date().time
        try {
            myRequestsList.reportFeedRequest(pathAndQuery)
            feedHandler.handleRequest(feedData, requestWrapper, response)
        } finally {
            myRequestsList.reportFeedRequestFinished(pathAndQuery, Date().time - startTime)
        }

        return null
    }

    private fun getPathComponents(request: HttpServletRequest): List<String> {
        val pathInfo = WebUtil.getPathWithoutAuthenticationType(request)
        val result = FEED_PATH_PATTERN.find(pathInfo)
        if (result != null) {
            val (feedPath, projectId, feedId, apiVersion) = result.destructured
            return listOf(feedPath, projectId, feedId, apiVersion.toUpperCase())
        }

        val version = TeamCityProperties.getProperty(NuGetFeedConstants.PROP_NUGET_API_VERSION, NUGET_API_V2)
        val apiVersion = if (NUGET_API_V2.equals(version, true)) "V2" else "V1"

        val defaultPathIndex = pathInfo.indexOf(NuGetServerSettings.DEFAULT_PATH_SUFFIX)
        return if (defaultPathIndex >= 0) listOf(
                pathInfo.substring(0, defaultPathIndex + NuGetServerSettings.DEFAULT_PATH_SUFFIX.length),
                NuGetFeedData.DEFAULT.projectId,
                NuGetFeedData.DEFAULT.feedId,
                apiVersion
        ) else listOf("", NuGetFeedData.DEFAULT.projectId, NuGetFeedData.DEFAULT.feedId, apiVersion)
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetFeedController::class.java.name)
        private val QUERY_ID = Regex("^(id=)(.*)", RegexOption.IGNORE_CASE)
        private val FEED_PATH_PATTERN = Regex("(.*" + NuGetServerSettings.PROJECT_PATH + "/([^/]+)/([^/]+)/(v[123]))")
        private val SEM_VER_LEVEL = Regex("(.*semVerLevel=)([\\d\\.]+)(.*)", RegexOption.IGNORE_CASE)
        private const val UNSUPPORTED_REQUEST = "Unsupported NuGet feed request"
        private const val NUGET_API_V2 = "v2"
    }
}
