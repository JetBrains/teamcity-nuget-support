package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class JsonRequestHandler : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
