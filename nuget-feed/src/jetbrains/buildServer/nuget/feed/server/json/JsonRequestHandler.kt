package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class JsonRequestHandler(serviceIndexHandler: JsonServiceIndexHandler,
                              searchQueryHandler: JsonSearchQueryHandler) : NuGetFeedHandler {
    private val myHandlers = HashMap<String, NuGetFeedHandler>()

    init {
        myHandlers["index.json"] = serviceIndexHandler
        myHandlers["query"] = searchQueryHandler
    }

    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val serviceName = request.pathInfo.splitToSequence("/").firstOrNull { it.isNotEmpty() }
        if (serviceName == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid request path")
            return
        }
        val handler = myHandlers[serviceName]
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid request path")
            return
        }
        handler.handleRequest(feedData, request, response)
    }
}
