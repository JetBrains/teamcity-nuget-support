package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.web.util.WebUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonServiceIndexHandler : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        if (request.pathInfo != "/index.json") {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
            return
        }

        val feedPathV3 = request.servletPath
        val feedPathV2 = feedPathV3.replaceAfterLast("/", "v2")
        val rootUrl = WebUtil.getRootUrl(request)

        val responseText = JsonServiceIndexHandler::class.java.getResourceAsStream("/feed-metadata/NuGet-V3.json").use {
            it.bufferedReader().readText()
        }

        response.status = HttpServletResponse.SC_OK
        response.contentType = "application/json;charset=UTF-8"
        response.writer.printf(responseText, rootUrl + feedPathV3, rootUrl + feedPathV2)
    }
}
