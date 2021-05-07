package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.web.util.WebUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter
import java.util.concurrent.Callable
import javax.servlet.http.HttpServletRequest

class TimeoutCallableInterceptor : CallableProcessingInterceptorAdapter() {
    override fun <T> handleTimeout(request: NativeWebRequest, task: Callable<T>): Any {
        val originalRequest = request.getNativeRequest(HttpServletRequest::class.java)
        if (originalRequest != null) {
            Loggers.SERVER.warn("Async request timeout. Request: " + WebUtil.getRequestDump(originalRequest))
        }
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(HttpStatus.REQUEST_TIMEOUT.reasonPhrase)
    }
}
