package jetbrains.buildServer.nuget.feed.server.controllers

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.web.context.request.async.AsyncWebRequest
import org.springframework.web.context.request.async.StandardServletAsyncWebRequest
import org.springframework.web.context.request.async.WebAsyncTask
import org.springframework.web.context.request.async.WebAsyncUtils
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.AsyncEvent
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AsyncRequestTaskImpl<T>(
        taskExecutor: ExecutorService,
        request: HttpServletRequest,
        response: HttpServletResponse,
        timeoutInSec: Long,
        handler: AsyncRequestHandler<T>) : AsyncRequestTask<T>
{
    private var mySingleTaskExecutor: SingleTaskExecutor
    private var myWebAsyncTask: WebAsyncTask<T>

    private val myAsyncState = AtomicInteger(ASYNC_NORMAL)
    private val myAsyncWebRequest: AsyncWebRequestWrapper

    private val myCleanupLock = Object()
    private val myState: AsyncRequestStateImpl = AsyncRequestStateImpl()

    init {
        myAsyncWebRequest = AsyncWebRequestWrapper(request, response)

        if (timeoutInSec > 0) {
            myState.setTimeout(getCurrentDateTime().plusSeconds(timeoutInSec))
            myAsyncWebRequest.setTimeout(2 * timeoutInSec * 1000)
        }

        mySingleTaskExecutor = SingleTaskExecutor(taskExecutor)

        myWebAsyncTask = WebAsyncTask<T>(null, mySingleTaskExecutor) {
            Thread.sleep(TeamCityProperties.getLong("teamcity.nuget.sleep", 0))
            val asyncManager = WebAsyncUtils.getAsyncManager(myAsyncWebRequest.request)
            handler.handle(myAsyncWebRequest.request.asyncContext, asyncManager.concurrentResultContext)
        }

        myAsyncWebRequest.setErrorEventHandler {
            myAsyncState.set(ASYNC_ERROR)
            if (myState.isCancellationRequested) return@setErrorEventHandler

            val request = (it?.suppliedRequest as HttpServletRequest?)
            LOG.warn("Async request completed with error. ${request?.let { WebUtil.getRequestDump(it) } }|${request?.requestURI}", it?.throwable)
        }

        myAsyncWebRequest.setTimeoutEventHandler {
            synchronized(myCleanupLock) {
                myAsyncState.set(ASYNC_TEMOUT)
            }

            val request = (it?.suppliedRequest as HttpServletRequest?)
            LOG.warn("Async request timed out: ${request?.let { WebUtil.getRequestDump(it) } }|${request?.requestURI}")
        }
    }

    public override val state: AsyncRequestState
        get() = myState

    public override val webAsyncTask: WebAsyncTask<T>
        get() = myWebAsyncTask

    public override val isDone: Boolean
        get() = mySingleTaskExecutor.feature.let { it == null || it.isDone }

    override val asyncWebRequest: AsyncWebRequest
        get() = myAsyncWebRequest

    public override fun cancel() {
        myState.cancelIfAllowed { }
    }

    public override fun interrupt() {
        myState.interruptIfAllowed {
            if (!myAsyncWebRequest.isAsyncComplete && myAsyncWebRequest.isAsyncStarted) {
                myAsyncWebRequest.request.asyncContext.timeout = 1
            }
        }
    }

    private fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())

    private class SingleTaskExecutor(private val executorService: ExecutorService) : AsyncTaskExecutor {
        private val myLock = Object()

        @Volatile
        private var myFuture: Future<*>? = null

        public val feature: Future<*>?
            get()= myFuture

        override fun submit(task: Runnable?): Future<*> {
            if (myFuture != null) return myFuture!!
            synchronized(myLock) {
                if (myFuture != null) return myFuture!!
                myFuture = executorService.submit(task)
                return myFuture!!
            }
        }

        override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
            throw NotImplementedError()
        }

        override fun execute(task: Runnable?, startTimeout: Long) {
            throw NotImplementedError()
        }

        override fun execute(task: Runnable) {
            throw NotImplementedError()
        }

    }

    private class AsyncWebRequestWrapper(request: HttpServletRequest, response: HttpServletResponse) : StandardServletAsyncWebRequest(request, response) {
        private var myTimeoutEventHandler: ((event: AsyncEvent?) -> Unit)? = null
        private var myErrorEventHandler: ((event: AsyncEvent?) -> Unit)? = null

        fun setTimeoutEventHandler(handler: (event: AsyncEvent?) -> Unit) {
            myTimeoutEventHandler = handler;
        }

        fun setErrorEventHandler(handler: (event: AsyncEvent?) -> Unit) {
            myErrorEventHandler = handler;
        }

        override fun onError(event: AsyncEvent?) {
            myErrorEventHandler?.let { it(event) }
            super.onError(event)
        }

        override fun onTimeout(event: AsyncEvent?) {
            myTimeoutEventHandler?.let { it(event) }
            super.onTimeout(event)
        }
    }

    private companion object {
        val LOG = Logger.getInstance(AsyncRequestTaskImpl::class.java.name)

        val ASYNC_NORMAL = 0
        val ASYNC_TEMOUT = 1
        val ASYNC_ERROR = 2
    }
}
