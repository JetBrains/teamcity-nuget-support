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
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandler
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandlerContext
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.executors.ExecutorsFactory
import jetbrains.buildServer.web.openapi.WebControllerManager
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.web.servlet.ModelAndView
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.servlet.*
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

    private var myExecutor: AsyncRequestExecutor
    private var myExecutorService: ExecutorService;
    private val myRequestSemaphore = Semaphore(
            TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_MAX_REQUESTS, NuGetFeedConstants.NUGET_FEED_MAX_REQUESTS),
            false)

    private val handleRequest: (HttpServletRequest, HttpServletResponse, String, (HttpServletRequest, HttpServletResponse) -> Unit) -> Unit
        get() = if (TeamCityProperties.getBooleanOrTrue(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_ENABLED)) ::handleRequestAsync
        else ::handleRequestSync

    init {
        setSupportedMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
        web.registerController(NuGetServerSettings.DEFAULT_PATH + "/**", this)
        web.registerController(NuGetServerSettings.PROJECT_PATH + "/**", this)

        myExecutorService = ExecutorsFactory.newFixedDaemonExecutor(
                "NuGet requests executor",
                0,
                TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_MAX_REQUESTS, NuGetFeedConstants.NUGET_FEED_MAX_REQUESTS),
                TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_QUEUE_CAPACITY, NuGetFeedConstants.NUGET_FEED_REQUEST_QUEUE_CAPACITY));

        myExecutor = AsyncRequestExecutor(
                myExecutorService,
                ExecutorsFactory.newFixedScheduledDaemonExecutor("NuGet requests timeouts", 1),
                1)
        myExecutor.start()
    }

    override fun doHandle(request: HttpServletRequest,
                          response: HttpServletResponse): ModelAndView? {
        if (!mySettings.isNuGetServerEnabled) {
            return NuGetResponseUtil.nugetFeedIsDisabled(response)
        }

        if (isPublishPackageServiceFeed(request)) {
            handlePublishPackageService(request, response)
            return null
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

        handleRequest(requestWrapper, response, feedPath) { handlerRequest, handlerResponse ->
            val feedData = NuGetFeedData(project.projectId, project.externalId, feedId)
            feedHandler.handleRequest(feedData, handlerRequest, handlerResponse)
        }

        return null
    }

    private fun handleRequestAsync(
            request: HttpServletRequest,
            response: HttpServletResponse,
            mappingPath: String,
            handler: (HttpServletRequest, HttpServletResponse) -> Unit
    ) {
        myExecutor.execute(request, response, object : AsyncRequestHandler {
            override fun handle(asyncContext: AsyncRequestExecutorContext) {
                val asyncRequest = asyncContext.request as HttpServletRequest
                val asyncResponse = asyncContext.response as HttpServletResponse
                val formattedRequestUrl = formatRequestUrl(asyncRequest, mappingPath)
                val startTime = Date().time
                try {
                    myRequestsList.reportFeedRequest(formattedRequestUrl)
                    handler(asyncRequest, asyncResponse)
                } finally {
                    myRequestsList.reportFeedRequestFinished(formattedRequestUrl, Date().time - startTime)
                }
            }

            override fun onRejected(asyncContext: AsyncRequestExecutorContext) {
                val asyncResponse = (asyncContext.response as HttpServletResponse)
                if (!asyncResponse.isCommitted) {
                    asyncResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
                }
            }

            override fun onError(asyncContext: AsyncRequestExecutorContext, throwable: Throwable) {
                val asyncResponse = (asyncContext.response as HttpServletResponse)
                if (!asyncResponse.isCommitted) {
                    asyncResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
                }
            }

            override fun onTimeout(asyncContext: AsyncRequestExecutorContext) {
                val asyncResponse = (asyncContext.response as HttpServletResponse)
                if (!asyncResponse.isCommitted) {
                    asyncResponse.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, SERVICE_UNAVAILABLE)
                }
            }
        }, 1)
    }

    private fun handleRequestSync(
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
        handleRequestAsync(requestWrapper, response, mappingPath) {
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
        private const val SERVICE_UNAVAILABLE = "NuGet feed temporary unavailable"
        private const val INTERNAL_SERVER_ERROR = "NuGet feed internal error"
        private const val NUGET_API_V2 = "v2"
    }

    interface  AsyncRequestExecutorContext {
        val request: ServletRequest
        val response: ServletResponse
    }

    interface AsyncRequestTimeoutHandler {
        fun onTimeout(asyncContext: AsyncRequestExecutorContext): Unit
    }

    interface AsyncRequestHandler : AsyncRequestTimeoutHandler {
        fun handle(asyncContext: AsyncRequestExecutorContext): Unit
        fun onRejected(asyncContext: AsyncRequestExecutorContext): Unit
        fun onError(asyncContext: AsyncRequestExecutorContext, throwable: Throwable): Unit
    }

    interface AsyncRequestState {
        val cancelScheduled : Boolean
            get
        val cancelling : Boolean
            get
        fun enterCancellable()
        fun leaveCancellable()
    }

    class AsyncRequestExecutor(
            private val myExecutorService: ExecutorService,
            private val myScheduledExecutorService: ScheduledExecutorService,
            private val myTimeoutInSec: Long
    ) {
        private val tasks = ConcurrentHashMap.newKeySet<TaskItem>();

        fun start() {
            myScheduledExecutorService.scheduleAtFixedRate({
                try {
                    val expiredTasks = tasks.filter { it.isExpired && it.canBeCancelled }.toList()
                    for (task in expiredTasks) {
                        if (tasks.remove(task)) {
                            if (task.isDone) continue
                            task.scheduleCancel()
                        }
                    }
                }
                catch(throwable: Throwable) {
                    LOG.warn("Exception has been occured in scheduler", throwable)
                }
            }, 0, 300, TimeUnit.MILLISECONDS)
        }

        fun execute(request: HttpServletRequest, response: HttpServletResponse, handler: AsyncRequestHandler, timeoutInSec: Long): Unit {
            var asyncContext = request.startAsync(request, response)
            val taskItem = TaskItem(asyncContext, timeoutInSec, handler)

            request.setAttribute(ASYNC_REQUEST_STATE, taskItem)

            asyncContext.addListener(taskItem)

            try {
                val future = myExecutorService.submit {
                    try {
                        Thread.sleep(TeamCityProperties.getLong("teamcity.nuget.sleep", 0))
                        handler.handle(AsyncRequestExecutorContextImpl(asyncContext))
                    }
                    catch(throwable: Throwable) {
                        if (taskItem.cancelScheduled) {
                            LOG.debug("Request has been cancelled due to timeout. Error in thread:", throwable)
                            //asyncContext.timeout = 1
                        } else {
                            LOG.warnAndDebugDetails("Error has been occured during processing async request", throwable)
                            try {
                                handler.onError(AsyncRequestExecutorContextImpl(asyncContext), throwable)
                            }
                            catch(throwable: Throwable) {
                                LOG.warnAndDebugDetails("Error has been occured", throwable)
                                val asyncResponse = (asyncContext.response as HttpServletResponse)
                                if (!asyncResponse.isCommitted) {
                                    asyncResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
                                }
                            }
                        }
                    }
                    finally {
                        request.removeAttribute(ASYNC_REQUEST_STATE)
                        if (!taskItem.cancelScheduled) {
                            try {
                                asyncContext.complete()
                            } catch (throwable: Throwable) {
                                LOG.warnAndDebugDetails("Error has been occured during completing async request", throwable)
                            }
                        }
                    }
                }
                
                taskItem.setFuture(future)
                tasks.add(taskItem)
            }
            catch(exception: RejectedExecutionException) {
                LOG.warnAndDebugDetails("Cannot start a new async request", exception)
                try {
                    handler.onRejected(AsyncRequestExecutorContextImpl(asyncContext))
                }
                catch(throwable: Throwable) {
                    LOG.warnAndDebugDetails("Error has been occured", throwable)
                    val asyncResponse = (asyncContext.response as HttpServletResponse)
                    if (!asyncResponse.isCommitted) {
                        asyncResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
                    }
                }
                asyncContext.complete()
            }
            catch(throwable: Throwable) {
                LOG.warnAndDebugDetails("Error has been occured during async request", throwable)
            }
        }

        class TaskItem(val asyncContext: AsyncContext, val timeoutInSec: Long, val timeoutHandler: AsyncRequestTimeoutHandler) : AsyncListener, AsyncRequestState {
            private val myFuture: AtomicReference<Future<*>?> = AtomicReference(null)
            private val myCancellableRegionDepth = AtomicLong(0)

            private var myTimeoutDateTime: LocalDateTime? = null

            @Volatile
            private var myCancelScheduled: Boolean = false

            @Volatile
            private var myCancelling: Boolean = false

            init {
                if (timeoutInSec > 0)
                    myTimeoutDateTime = getCurrentDateTime().plusSeconds(timeoutInSec)
            }

            public override val cancelScheduled: Boolean
                get() = myCancelScheduled

            public val canBeCancelled: Boolean
                get() = myCancellableRegionDepth.get() > 0

            override val cancelling: Boolean
                get() = myCancelling

            public val isExpired: Boolean
                get() = myTimeoutDateTime != null && myTimeoutDateTime!! <= getCurrentDateTime()

            public val isDone: Boolean
                get() = myFuture.get().let { it == null || it.isDone }

            public val future: Future<*>?
                get() = myFuture.get()

            public fun setFuture(future: Future<*>) {
                if (!myFuture.compareAndSet(null, future))
                    throw IllegalStateException("future should be set once")
            }

            public fun scheduleCancel() {
                myCancelScheduled = true
                myFuture.get()?.cancel(true)
            }

            override fun enterCancellable() {
                myCancellableRegionDepth.incrementAndGet()
            }

            override fun leaveCancellable() {
                val depth = myCancellableRegionDepth.get()
                if (depth == 0L) throw IllegalStateException("leaveCancellable call number shoul be equal to enterCancellable call number. Value ${depth} ")

                if (Thread.currentThread().isInterrupted) {
                    throw InterruptedException()
                }

                if (myCancellableRegionDepth.decrementAndGet() != 0L) return

                if (myCancelScheduled) {
                    myCancelling = true
                    throw TimeoutException()
                    //Thread.currentThread().interrupt()
                    //future?.cancel(true)
                }
            }

            override fun onComplete(event: AsyncEvent?) {
                if (LOG.isDebugEnabled) {
                    val request = (event?.suppliedRequest as HttpServletRequest?)
                    LOG.info("Async request completed: ${request?.let { WebUtil.getRequestDump(it)} }|${request?.requestURI}")
                }
            }

            override fun onStartAsync(event: AsyncEvent?) {
                if (LOG.isDebugEnabled) {
                    val request = (event?.suppliedRequest as HttpServletRequest?)
                    LOG.info("Async request started: ${request?.let { WebUtil.getRequestDump(it)} }|${request?.requestURI}")
                }
            }

            override fun onTimeout(event: AsyncEvent?) {
                val request = (event?.suppliedRequest as HttpServletRequest?)
                LOG.warn("Async request timed out: ${request?.let { WebUtil.getRequestDump(it)} }|${request?.requestURI}")

                try {
                    timeoutHandler.onTimeout(AsyncRequestExecutorContextImpl(asyncContext))
                }
                catch (throwable: Throwable) {
                    // LOG
                }
                finally {
                    asyncContext.complete()
                }
            }

            override fun onError(event: AsyncEvent?) {
                if (myCancelScheduled) return

                val request = (event?.suppliedRequest as HttpServletRequest?)
                LOG.warn("Async request completed with error. ${request?.let { WebUtil.getRequestDump(it)} }|${request?.requestURI}")
            }

            private fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())
        }

        class AsyncRequestExecutorContextImpl(private val context: AsyncContext) : AsyncRequestExecutorContext {
            override val request: ServletRequest
                get() = context.request
            override val response: ServletResponse
                get() = context.response
        }

        companion object {
            public val ASYNC_REQUEST_STATE = "TC.AsyncRequest.State"

            public fun getAsyncRequestState(request: HttpServletRequest) : AsyncRequestState? {
                return request.getAttribute(ASYNC_REQUEST_STATE) as AsyncRequestState?
            }

            public fun getAsyncRequestStateOrDefault(request: HttpServletRequest) : AsyncRequestState {
                return (request.getAttribute(ASYNC_REQUEST_STATE) as AsyncRequestState?) ?: NoAsyncState
            }

            public val NoAsyncState : AsyncRequestState = object : AsyncRequestState {
                override val cancelScheduled: Boolean
                    get() = false

                override val cancelling: Boolean
                    get() = false

                override fun enterCancellable() {
                }

                override fun leaveCancellable() {
                }
            }
        }
    }
}
