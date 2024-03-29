

package jetbrains.buildServer.nuget.feed.server.controllers

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RecentNuGetRequests
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RequestWrapper
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandler
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandlerContext
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.WebControllerManager
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.web.servlet.ModelAndView
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
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
                          private val myRepositoryManager: RepositoryManager,
                          private val myServiceFeedHandler: NuGetServiceFeedHandler)
    : BaseController() {

    val myRequestSemaphore = Semaphore(
            TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_MAX_REQUESTS, NuGetFeedConstants.NUGET_FEED_MAX_REQUESTS),
            false)

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

        if (isPublishPackageServiceFeed(request)) {
            return handlePublishPackageService(request, response)
        }

        val (feedPath, projectId, feedId, apiMethod) = getPathComponents(request)
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

        val requestWrapper = createRequestWrapper(request, feedPath)

        // Process package download request
        if (apiMethod == "DOWNLOAD") {
            val artifactDownloadUrl = "/repository/download${getRelativeRequestPath(requestWrapper, feedPath)}"
            val dispatcher = request.getRequestDispatcher(artifactDownloadUrl)
            if (dispatcher != null) {
                LOG.debug(String.format("Forwarding download package request from %s to %s", getRequestPath(requestWrapper), artifactDownloadUrl))
                dispatcher.forward(request, response)
            }
            return null
        }

        // Set NuGet feed API version
        requestWrapper.setAttribute(NuGetFeedConstants.NUGET_FEED_API_VERSION, NuGetAPIVersion.valueOf(apiMethod))

        val feedHandler = myFeedProvider.getHandler(requestWrapper)
        if (feedHandler == null) {
            LOG.debug(String.format("%s: %s", UNSUPPORTED_REQUEST, formatRequestUrl(requestWrapper, feedPath)))
            // error response according to OData spec for unsupported operations (modification operations)
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, UNSUPPORTED_REQUEST)
            return null
        }

        handleRequest(requestWrapper, response, feedPath) {
            handlerRequest, handlerResponse ->
            val feedData = NuGetFeedData(project.projectId, project.externalId, feedId)
            feedHandler.handleRequest(feedData, handlerRequest, handlerResponse)
        }

        return null
    }

    private fun handleRequest(
            request: HttpServletRequest,
            response: HttpServletResponse,
            mappingPath: String,
            handler: (HttpServletRequest, HttpServletResponse) -> Unit
    ) {
        val formattedRequestUrl = formatRequestUrl(request, mappingPath)
        val startTime = Date().time
        try {
            myRequestsList.reportFeedRequest(formattedRequestUrl)

            val timeout = TeamCityProperties.getLong(
                    NuGetFeedConstants.PROP_NUGET_FEED_REQUEST_PENDING_PROCESSING_TIMEOUT,
                    NuGetFeedConstants.NUGET_FEED_REQUEST_PENDING_PROCESSING_TIMEOUT)
            if (myRequestSemaphore.tryAcquire(timeout, TimeUnit.SECONDS)) {
                try {
                    handler(request, response)
                }
                finally {
                    myRequestSemaphore.release()
                }
            } else {
                LOG.warn("Could not start to process NuGet reqest during $timeout sec, request: ${WebUtil.getRequestDump(request)}|${request.requestURI}")
                response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, REQUEST_TIMEOUT)
            }
        } finally {
            myRequestsList.reportFeedRequestFinished(formattedRequestUrl, Date().time - startTime)
        }
    }

    private fun createRequestWrapper(request: HttpServletRequest, mappingPath: String): RequestWrapper {
        return object : RequestWrapper(request, mappingPath) {
            override fun getQueryString(): String? {
                val queryString = super.getQueryString()
                return if (queryString == null || !super.getRequestURI().endsWith("FindPackagesById()")) {
                    queryString
                } else {
                    // NuGet client in VS 2015 Update 2 introduced breaking change where
                    // instead of `id` parameter passed `Id` while OData is case sensitive
                    QUERY_ID.replaceFirst(queryString, "id=$2")
                }
            }
        }
    }

    private fun getRequestPath(requestWrapper: HttpServletRequest) : String {
        var requestPath = WebUtil.getPathWithoutAuthenticationType(requestWrapper)
        if (!requestPath.startsWith("/")) requestPath = "/$requestPath"
        return requestPath
    }

    private fun getRelativeRequestPath(requestWrapper: HttpServletRequest, mappingPath: String) : String {
        val requestPath = getRequestPath(requestWrapper)
        return requestPath.substring(mappingPath.length)
    }

    private fun formatRequestUrl(requestWrapper: HttpServletRequest, mappingPath: String): String {
        val query = requestWrapper.queryString
        val path = getRelativeRequestPath(requestWrapper, mappingPath)
        return "${requestWrapper.method} $path" + if (query != null) "?$query" else ""
    }

    private fun getPathComponents(request: HttpServletRequest): List<String> {
        val pathInfo = WebUtil.getPathWithoutAuthenticationType(request)

        // Try to match per-project feed reference, e.g. /app/nuget/feed/_Root/default/...
        val result = FEED_PATH_PATTERN.find(pathInfo)
        if (result != null) {
            val (feedPath, projectId, feedId, apiVersion) = result.destructured
            return listOf(feedPath, projectId, feedId, apiVersion.toUpperCase())
        }

        // Try to handle request to global feed, e.g. /app/nuget/v1/FeedService.svc/...
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

    private fun isPublishPackageServiceFeed(request: HttpServletRequest): Boolean {
        val pathInfo = WebUtil.getPathWithoutAuthenticationType(request)
        return SERVICE_FEED_PATH_PATTERN.matches(pathInfo)
    }

    private fun handlePublishPackageService(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
        val pathInfo = WebUtil.getPathWithoutAuthenticationType(request)
        val pathInfoMatch = SERVICE_FEED_PATH_PATTERN.find(pathInfo)
        if (pathInfoMatch == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid request to NuGet Feed")
            return null
        }

        val (mappingPath, projectId) = pathInfoMatch.destructured
        if (mappingPath.isEmpty() || projectId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid request to NuGet Feed")
            return null
        }

        val project = myProjectManager.findProjectByExternalId(projectId)
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "NuGet Feed project $projectId not found")
            return null
        }

        val requestWrapper = createRequestWrapper(request, mappingPath)
        handleRequest(requestWrapper, response, mappingPath) {
            handlerRequest, handlerResponse ->
                val context = object : NuGetServiceFeedHandlerContext {
                    override val projectId: String
                        get() = projectId
                }
                myServiceFeedHandler.handleRequest(context, handlerRequest, handlerResponse)
        }

        return null
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetFeedController::class.java.name)
        private val QUERY_ID = Regex("^(id=)(.*)", RegexOption.IGNORE_CASE)
        private val FEED_PATH_PATTERN = Regex("(.*" + NuGetServerSettings.PROJECT_PATH + "/([^/]+)/([^/]+)/(v[123]|download))")
        private val SERVICE_FEED_PATH_PATTERN = Regex("(.*" + NuGetServerSettings.SERVICE_FEED_PATH + "/([^/]+)/)", RegexOption.IGNORE_CASE)
        private const val UNSUPPORTED_REQUEST = "Unsupported NuGet feed request"
        private const val REQUEST_TIMEOUT = "NuGet feed request timeout"
        private const val NUGET_API_V2 = "v2"
    }
}
