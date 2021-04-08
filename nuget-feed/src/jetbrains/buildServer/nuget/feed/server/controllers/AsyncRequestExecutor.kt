package jetbrains.buildServer.nuget.feed.server.controllers

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.controllers.AsyncRequestStateImpl.Companion.NoAsyncState
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.*
import java.util.concurrent.*
import javax.servlet.DispatcherType
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AsyncRequestExecutor(
        private val myExecutorService: ExecutorService,
        private val myScheduledExecutorService: ScheduledExecutorService,
        private val timeoutTaskManager: AsyncRequestTaskTimeoutManager
) {
    private val myInterceptors = ArrayList<CallableProcessingInterceptor>()

    fun start() {
        myScheduledExecutorService.scheduleAtFixedRate({
            try {
                timeoutTaskManager.processTasks()
            }
            catch(throwable: Throwable) {
                LOG.warn("Exception has been occured in scheduler", throwable)
            }
        }, 0, 300, TimeUnit.MILLISECONDS)
    }

    fun registerCallableInterceptors(vararg interceptors: CallableProcessingInterceptor) {
        myInterceptors.addAll(interceptors)
    }

    fun <T> execute(request: HttpServletRequest, response: HttpServletResponse, handler: AsyncRequestHandler<T>, timeoutInSec: Long, context: Array<Any>?): Unit {
        if (request.dispatcherType == DispatcherType.ASYNC) {
            handleAsyncResult(request, response, handler)
            return
        }
        val asyncRequestTask = AsyncRequestTaskImpl(myExecutorService, request, response, timeoutInSec, handler)
        val asyncManager = WebAsyncUtils.getAsyncManager(request)
        asyncManager.setAsyncWebRequest(asyncRequestTask.asyncWebRequest)
        asyncManager.registerCallableInterceptors(object : CallableProcessingInterceptorAdapter() {
            override fun <T : Any?> preProcess(request: NativeWebRequest?, task: Callable<T>?) {
                TASK_ITEM.set(asyncRequestTask.state)
                timeoutTaskManager.registerTask(asyncRequestTask)
            }

            override fun <T : Any?> postProcess(request: NativeWebRequest?, task: Callable<T>?, concurrentResult: Any?) {
                TASK_ITEM.remove()
            }
        })
        asyncManager.registerCallableInterceptors(*myInterceptors.toTypedArray())
        asyncManager.startCallableProcessing(asyncRequestTask.webAsyncTask, context)
    }

    private fun <T> handleAsyncResult(request: HttpServletRequest, response: HttpServletResponse, handler: AsyncRequestHandler<T>) {
        val asyncManager = WebAsyncUtils.getAsyncManager(request)
        if (asyncManager.hasConcurrentResult()) {
            if (asyncManager.concurrentResult is Throwable && !response.isCommitted) {
                if (asyncManager.concurrentResult is AsyncRequestTimeoutException) {
                    handler.onTimeout(request, response)
                } else {
                    handler.onError(request, response, asyncManager.concurrentResult as Throwable)
                }
            }
        }
    }

    companion object {
        private val TASK_ITEM = ThreadLocal<AsyncRequestState>()
        private val LOG = Logger.getInstance(AsyncRequestExecutor::class.java.name)
        private val SERVICE_UNAVAILABLE = "Service Unavailable"
        private val INTERNAL_SERVER_ERROR = "Internal Server Error"

        public fun getAsyncRequestState() : AsyncRequestState? {
            return TASK_ITEM.get()
        }

        public fun getAsyncRequestStateOrDefault() : AsyncRequestState {
            return TASK_ITEM.get() ?: NoAsyncState
        }
    }
}
