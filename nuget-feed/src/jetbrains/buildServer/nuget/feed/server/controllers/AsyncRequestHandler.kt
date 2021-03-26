package jetbrains.buildServer.nuget.feed.server.controllers

import javax.servlet.AsyncContext

interface AsyncRequestHandler {
    fun handle(asyncContext: AsyncContext): Unit
    fun onRejected(asyncContext: AsyncContext): Unit
    fun onError(asyncContext: AsyncContext, throwable: Throwable): Unit
    fun onTimeout(asyncContext: AsyncContext): Unit
}

interface AsyncRequestState {
    val isCancellationRequested : Boolean
        get
    val isCancelling : Boolean
        get

    fun throwIfCancellationRequested()
    fun acceptCancelling()
}
