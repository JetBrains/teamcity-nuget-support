package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.serverSide.TeamCityProperties
import org.springframework.web.util.UriUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object DispatcherUtils {
    fun isAsyncEnabled(): Boolean {
        return TeamCityProperties.getBooleanOrTrue(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_ENABLED)
    }

    fun setContext(request: HttpServletRequest, context: JsonNuGetFeedContext) {
        request.setAttribute(NuGetFeedConstants.NUGET_FEED_ASYNC_DATA_CONTEXT, context)
    }

    fun getContext(request: HttpServletRequest): JsonNuGetFeedContext? {
        return request.getAttribute(NuGetFeedConstants.NUGET_FEED_ASYNC_DATA_CONTEXT) as JsonNuGetFeedContext?
    }

    fun dispatchGetRegistrations(request: HttpServletRequest, response: HttpServletResponse, context: JsonNuGetFeedContext, id: String) {
        setContext(request, context)

        val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_PACKAGE_REGISTRATIONS}/${encode(id)}")
        dispatcher.forward(request, response)
    }

    fun dispatchGetRegistration(request: HttpServletRequest, response: HttpServletResponse, context: JsonNuGetFeedContext, id: String, version: String) {
        setContext(request, context)

        val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_PACKAGE_REGISTRATIONS}/${encode(id)}/${encode(version)}")
        dispatcher.forward(request, response)
    }

    fun dispatchSearchPackages(request: HttpServletRequest, response: HttpServletResponse, context: JsonNuGetFeedContext) {
        setContext(request, context)

        val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_PACKAGE_SEARCH}")
        dispatcher.forward(request, response)
    }

    fun dispatchGetPackageVersions(request: HttpServletRequest, response: HttpServletResponse, context: JsonNuGetFeedContext, id: String) {
        setContext(request, context)

        val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_PACKAGE_VERSIONS}/${encode(id)}")
        dispatcher.forward(request, response)
    }

    fun dispatchGetPackageContent(request: HttpServletRequest, response: HttpServletResponse, context: JsonNuGetFeedContext, id: String, version: String, extension: String) {
        setContext(request, context)

        val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_PACKAGE_CONTENT}/${encode(id)}/${encode(version)}/${encode(extension)}")
        dispatcher.forward(request, response)
    }

    private fun encode(pathSegment: String): String {
        return UriUtils.encodePathSegment(pathSegment, "UTF-8")
    }
}
