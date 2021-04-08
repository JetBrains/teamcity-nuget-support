package jetbrains.buildServer.nuget.feed.server.controllers

import org.springframework.web.context.request.async.AsyncWebRequest
import org.springframework.web.context.request.async.WebAsyncTask
import java.time.LocalDateTime
import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface AsyncRequestHandler<T> {
    fun handle(asyncContext: AsyncContext, context: Array<Any>?): T
    fun onRejected(request: HttpServletRequest, response: HttpServletResponse): Unit
    fun onError(request: HttpServletRequest, response: HttpServletResponse, throwable: Throwable): Unit
    fun onTimeout(request: HttpServletRequest, response: HttpServletResponse): Unit
}

interface AsyncRequestState {
    val isExpired: Boolean
    val isCancellationRequested : Boolean
    val isCancelling : Boolean
    val isCancellingExpired: Boolean
    val canBeCancelled: Boolean
    val canBeInterrupted: Boolean

    fun throwIfCancellationRequested()
    fun acceptCancelling()
}

interface AsyncRequestStateController {
    fun cancelIfAllowed(action: () -> Unit)
    fun interruptIfAllowed(action: () -> Unit)
    fun setTimeout(timeoutDateTime: LocalDateTime)
}

interface AsyncRequestTask<T> {
    val state: AsyncRequestState
    val isDone: Boolean

    val webAsyncTask: WebAsyncTask<T>
    val asyncWebRequest: AsyncWebRequest

    fun cancel()
    fun interrupt()
}

interface AsyncRequestTaskTimeoutManager {
    fun registerTask(task: AsyncRequestTask<*>)
    fun processTasks()
}
