package jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed

import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandlerContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface NuGetServiceFeedHandler {
    @Throws(Exception::class)
    fun handleRequest(context: NuGetServiceFeedHandlerContext, request: HttpServletRequest, response: HttpServletResponse)
}

