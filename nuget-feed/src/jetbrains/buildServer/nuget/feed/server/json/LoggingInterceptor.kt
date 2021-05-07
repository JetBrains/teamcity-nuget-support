package jetbrains.buildServer.nuget.feed.server.json

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RecentNuGetRequests
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter
import java.lang.System.currentTimeMillis
import java.util.concurrent.Callable
import javax.servlet.http.HttpServletRequest

class LoggingInterceptor(
        private val nugetRequests: RecentNuGetRequests
) : CallableProcessingInterceptorAdapter() {
    private var myStartDateTimeMs: Long? = null
    private var myFeedUrlDesc: String? = null

    override fun <T : Any?> preProcess(request: NativeWebRequest?, task: Callable<T>?) {
        try {
            request?.getNativeRequest(HttpServletRequest::class.java)?.let {
                var requestPath = WebUtil.getPathWithoutAuthenticationType(it)
                if (!requestPath.startsWith("/")) requestPath = "/$requestPath"

                val query = it.queryString
                myFeedUrlDesc = "${it.method} $requestPath" + if (query != null) "?$query" else ""
                myStartDateTimeMs = currentTimeMillis()

                nugetRequests.reportFeedRequest(myFeedUrlDesc!!)
            }
        } catch (throwable: Throwable) {
            LOG.warnAndDebugDetails("Error when starting measurement of NuGet request duration.", throwable)
        }
    }

    override fun <T : Any?> postProcess(request: NativeWebRequest?, task: Callable<T>?, concurrentResult: Any?) {
        try {
            reportRequestTime()
        } catch (throwable: Throwable) {
            LOG.warnAndDebugDetails("Error when ending measurement of NuGet request duration.", throwable)
        }
    }

    override fun <T : Any?> handleTimeout(request: NativeWebRequest?, task: Callable<T>?): Any {
        try {
            request?.getNativeRequest(HttpServletRequest::class.java)?.let {
                LOG.warn("Async request timeout. Request: ${WebUtil.getRequestDump(it)}")
            }
            reportRequestTime()
        } catch (throwable: Throwable) {
            LOG.warnAndDebugDetails("Error when ending (by timeout) measurement of NuGet request duration.", throwable)
        }

        return super.handleTimeout(request, task)
    }

    private fun reportRequestTime() {
        if (myFeedUrlDesc == null || myStartDateTimeMs == null) return

        nugetRequests.reportFeedRequestFinished(myFeedUrlDesc!!, currentTimeMillis() - myStartDateTimeMs!!)
    }

    private companion object {
        val LOG = Logger.getInstance(LoggingInterceptor::class.java.name)
    }
}
