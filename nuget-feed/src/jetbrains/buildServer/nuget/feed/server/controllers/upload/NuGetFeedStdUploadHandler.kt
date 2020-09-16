package jetbrains.buildServer.nuget.feed.server.controllers.upload

import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class NuGetFeedStdUploadHandler(
        private val myUploadHandler: PackageUploadHandler<NuGetFeedUploadHandlerStdContext>
) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        myUploadHandler.handleRequest(NuGetFeedUploadHandlerStdContextImpl(feedData), request, response)
    }
}
