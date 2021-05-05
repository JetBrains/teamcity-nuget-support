package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.serverSide.TeamCityProperties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonSearchQueryHandler(
        private val feedFactory: NuGetFeedFactory,
        private val packageSourceFactory: JsonPackageSourceFactory,
        private val adapterFactory: JsonPackageAdapterFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val feed = feedFactory.createFeed(feedData)
        val context = JsonNuGetFeedContext(feed, request)

        val asyncEnabled = TeamCityProperties.getBoolean(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_ENABLED)
        if (asyncEnabled) {
            DispatcherUtils.dispatchSearchPackages(request, response, context)
        } else {
            searchPackages(request, response, context)
        }
    }

    private fun searchPackages(request: HttpServletRequest, response: HttpServletResponse, context: JsonNuGetFeedContext) {
        val query = request.getParameter("q")
        val skip = request.getParameter("skip")?.toIntOrNull()
        val take = request.getParameter("take")?.toIntOrNull() ?: NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE
        val prerelease = request.getParameter("prerelease")?.toBoolean() ?: false
        val includeSemVer2 = request.includeSemVer2()

        val packageSource = packageSourceFactory.create(context.feed)
        val packages = packageSource.searchPackages(query, prerelease, includeSemVer2)

        val adapter = adapterFactory.create(context)
        response.writeJson(adapter.createSearchPackagesResponse(packages, take, skip))
    }
}
