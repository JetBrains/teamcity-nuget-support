package jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandler
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload.NuGetServiceFeedUploadHandlerContext
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload.NuGetServiceFeedUploadHandlerContextImpl
import jetbrains.buildServer.web.util.WebUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NuGetServiceFeedHandlerImpl(
        private val myUploadHandler: NuGetFeedUploadHandler<NuGetServiceFeedUploadHandlerContext>
) : NuGetServiceFeedHandler {

    override fun handleRequest(context: NuGetServiceFeedHandlerContext, request: HttpServletRequest, response: HttpServletResponse) {
        if (!"put".equals(request.method, true)) {
            LOG.warn("Unsuported request to service feed: ${WebUtil.getRequestDump(request)}|${request.requestURI}")
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, UNSUPPORTED_REQUEST)
            return
        }

        myUploadHandler.handleRequest(NuGetServiceFeedUploadHandlerContextImpl(context), request, response)
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetServiceFeedHandlerImpl::class.java.name)
        private const val UNSUPPORTED_REQUEST = "Unsupported NuGet service feed request"
    }
}
