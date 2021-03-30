package jetbrains.buildServer.nuget.feed.server.controllers

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.web.AsyncContextNotifier
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.context.request.async.StandardServletAsyncWebRequest
import org.springframework.web.context.request.async.WebAsyncUtils
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Lock
import javax.servlet.AsyncContext
import javax.servlet.AsyncEvent
import javax.servlet.AsyncListener
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AsyncRequestExecutor(
        private val myExecutorService: ExecutorService,
        private val myScheduledExecutorService: ScheduledExecutorService,
        private val myTimeoutInSec: Long
) {
    private val tasks = ConcurrentHashMap.newKeySet<TaskItem>();

    fun start() {
        myScheduledExecutorService.scheduleAtFixedRate({
            try {
                val expiredTasksPhaseOne = tasks.filter { it.isExpired && it.canBeCancelled }.toList()
                for (task in expiredTasksPhaseOne) {
                    if (task.isDone) {
                        tasks.remove(task)
                    } else {
                        task.cancel()
                    }
                }

                val expiredTasksPhaseTwo = tasks.filter { it.isCancellingExpired && it.canBeInterrupted }.toList()
                for (task in expiredTasksPhaseTwo) {
                    if (tasks.remove(task)) {
                        if (!task.isDone) {
                            task.interrupt()
                        }
                    }
                }

                val cancellingTasks = tasks.filter { it.isCancelling }.toList()
                for (task in cancellingTasks) {
                    tasks.remove(task)
                }
            }
            catch(throwable: Throwable) {
                LOG.warn("Exception has been occured in scheduler", throwable)
            }
        }, 0, 300, TimeUnit.MILLISECONDS)
    }

    fun execute(request: HttpServletRequest, response: HttpServletResponse, handler: AsyncRequestHandler, timeoutInSec: Long): Unit {
        val asyncResult = startDeferredResultProcessing(request, response)
        val taskItem = TaskItem(request.asyncContext, asyncResult, timeoutInSec, handler)

        try {
            val future = myExecutorService.submit {
                try {
                    setupThread(taskItem)
                    
                    Thread.sleep(TeamCityProperties.getLong("teamcity.nuget.sleep", 0))

                    taskItem.execute();
                }
                catch(throwable: Throwable) {
                    if (taskItem.isCancellationRequested) {
                        LOG.debug("Request has been cancelled due to timeout. Error in thread:", throwable)
                    } else {
                        LOG.warnAndDebugDetails("Error has been occured during processing async request", throwable)
                        try {
                            handler.onError(taskItem.asyncContext, throwable)
                        }
                        catch(throwable: Throwable) {
                            LOG.warnAndDebugDetails("Error has been occured", throwable)
                            val asyncResponse = (taskItem.asyncContext.response as HttpServletResponse)
                            if (!asyncResponse.isCommitted) {
                                asyncResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
                            }
                        }
                    }
                }
                finally {
                    try {
                        taskItem.complete()
                    }
                    finally {
                        cleanupThread()
                    }
                }
            }

            taskItem.setFuture(future)
            tasks.add(taskItem)
        }
        catch(exception: RejectedExecutionException) {
            LOG.warnAndDebugDetails("Cannot start a new async request", exception)
            try {
                handler.onRejected(taskItem.asyncContext)
            }
            catch(throwable: Throwable) {
                LOG.warnAndDebugDetails("Error has been occured", throwable)
                val asyncResponse = (taskItem.asyncContext.response as HttpServletResponse)
                if (!asyncResponse.isCommitted) {
                    asyncResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
                }
            }
            taskItem.asyncContext.complete()
        }
        catch(throwable: Throwable) {
            LOG.warnAndDebugDetails("Error has been occured during async request", throwable)
            throw throwable
        }
    }

    private fun startDeferredResultProcessing(request: HttpServletRequest, response: HttpServletResponse): DeferredResult<Any> {
        val asyncResult = DeferredResult<Any>()
        val asyncManager = WebAsyncUtils.getAsyncManager(request)
        val asyncWebRequest = StandardServletAsyncWebRequest(request, response)

        asyncManager.setAsyncWebRequest(asyncWebRequest)
        asyncManager.startDeferredResultProcessing(asyncResult)

        return asyncResult
    }

    private fun setupThread(taskItem: TaskItem) {
        TASK_ITEM.set(taskItem)
    }

    private fun cleanupThread() {
        TASK_ITEM.remove()
    }

    class TaskItem(val asyncContext: AsyncContext, val asyncResult: DeferredResult<Any>, val timeoutInSec: Long, val handler: AsyncRequestHandler) : AsyncListener, AsyncRequestState {
        private var myNotifier: AsyncContextNotifier?
        private val myFuture: AtomicReference<Future<*>?> = AtomicReference(null)
        private val myState = AtomicInteger(NORMAL)
        private val myAsyncState = AtomicInteger(ASYNC_NORMAL)
        private val myCleanupLock = Object()

        @Volatile
        private var myTimeoutDateTime: LocalDateTime? = null
        @Volatile
        private var myCancellingTimeoutDateTime: LocalDateTime? = null

        init {
            if (timeoutInSec > 0) {
                myTimeoutDateTime = getCurrentDateTime().plusSeconds(timeoutInSec)
                asyncContext.timeout = 2 * timeoutInSec * 1000
            }

            asyncContext.addListener(this)

            myNotifier = asyncContext.request.getAttribute(AsyncContextNotifier.ASYNC_CONTEXT_NOTIFIER) as AsyncContextNotifier?
        }

        public val canBeCancelled: Boolean
            get() = myState.get() == NORMAL

        public val canBeInterrupted: Boolean
            get() = myState.get() == CANCELLATION_REQUESTED

        public val isExpired: Boolean
            get() = myTimeoutDateTime != null && myTimeoutDateTime!! <= getCurrentDateTime()

        public val isCancellingExpired: Boolean
            get() = myCancellingTimeoutDateTime != null && myCancellingTimeoutDateTime!! <= getCurrentDateTime()

        public val isDone: Boolean
            get() = myFuture.get().let { it == null || it.isDone }

        public val future: Future<*>?
            get() = myFuture.get()

        public override val isCancellationRequested: Boolean
            get() = myState.get() >= CANCELLATION_REQUESTED

        public override val isCancelling: Boolean
            get() = myState.get() >= CANCELLING

        public fun setFuture(future: Future<*>) {
            if (!myFuture.compareAndSet(null, future))
                throw IllegalStateException("future should be set once")
        }

        public fun cancel() {
            if (myState.compareAndSet(NORMAL, CANCELLATION_REQUESTED)) {
                myCancellingTimeoutDateTime = getCurrentDateTime().plusSeconds(1)
            }
        }

        public fun interrupt() {
            if (myState.compareAndSet(CANCELLATION_REQUESTED, INTERRUPTING)) {
                asyncContext.timeout = 1
            }
        }

        public fun complete() {
            synchronized(myCleanupLock) {
                val isTimeout = myAsyncState.get() == ASYNC_TEMOUT
                if (isTimeout) {
                    myNotifier?.fireCleanupThread(AsyncEvent(asyncContext))
                } else {
                    myNotifier?.fireCleanupThread(AsyncEvent(asyncContext, asyncContext.request, asyncContext.response))
                }
            }

            if (myAsyncState.get() == ASYNC_NORMAL) {
                asyncResult.setResult(null)
            }
        }

        public fun execute() {
            myNotifier?.fireSetupThread(AsyncEvent(asyncContext, asyncContext.request, asyncContext.response))

            handler.handle(asyncContext)
        }

        override fun acceptCancelling() {
            myState.compareAndSet(CANCELLATION_REQUESTED, CANCELLING)
        }

        override fun throwIfCancellationRequested() {
            myState.compareAndSet(CANCELLATION_REQUESTED, CANCELLING)
            if (isCancellationRequested) {
                throw CancellationException()
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
            synchronized(myCleanupLock) {
                myAsyncState.set(ASYNC_TEMOUT)
            }

            val request = (event?.suppliedRequest as HttpServletRequest?)
            LOG.warn("Async request timed out: ${request?.let { WebUtil.getRequestDump(it)} }|${request?.requestURI}")

            future?.cancel(true)

            try {
                handler.onTimeout(asyncContext)
            }
            catch (throwable: Throwable) {
                // LOG
            }
            finally {
                asyncContext.complete()
            }
        }

        override fun onError(event: AsyncEvent?) {
            myAsyncState.set(ASYNC_ERROR)
            if (isCancellationRequested) return

            val request = (event?.suppliedRequest as HttpServletRequest?)
            LOG.warn("Async request completed with error. ${request?.let { WebUtil.getRequestDump(it)} }|${request?.requestURI}", event?.throwable)
        }

        private fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())

        private companion object {
            val NORMAL = 0
            val CANCELLATION_REQUESTED = 1
            val CANCELLING = 2
            val INTERRUPTING = 3

            val ASYNC_NORMAL = 0
            val ASYNC_TEMOUT = 1
            val ASYNC_ERROR = 2
        }
    }

    companion object {
        private val TASK_ITEM = ThreadLocal<TaskItem>()
        private val LOG = Logger.getInstance(AsyncRequestExecutor::class.java.name)
        private val SERVICE_UNAVAILABLE = "Service Unavailable"
        private val INTERNAL_SERVER_ERROR = "Internal Server Error"

        public fun getAsyncRequestState() : AsyncRequestState? {
            return TASK_ITEM.get()
        }

        public fun getAsyncRequestStateOrDefault() : AsyncRequestState {
            return TASK_ITEM.get() ?: NoAsyncState
        }

        public val NoAsyncState : AsyncRequestState = object : AsyncRequestState {
            override val isCancellationRequested: Boolean
                get() = false

            override val isCancelling: Boolean
                get() = false

            override fun throwIfCancellationRequested() {
            }

            override fun acceptCancelling() {
            }
        }
    }
}
