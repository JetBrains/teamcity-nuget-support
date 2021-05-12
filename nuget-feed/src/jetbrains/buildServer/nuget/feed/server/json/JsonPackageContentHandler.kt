package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.serverSide.TeamCityProperties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonPackageContentHandler(
        private val feedFactory: NuGetFeedFactory,
        private val packageSourceFactory: JsonPackageSourceFactory,
        private val adapterFactory: JsonPackageAdapterFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val matchResult = FLAT_CONTAINER_URL.find(request.pathInfo)
        if (matchResult == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
            return
        }

        val (id, version, _, file, extension) = matchResult.destructured
        val feed = feedFactory.createFeed(feedData)
        val context = JsonNuGetFeedContext(feed, request)

        if (version == "index.json" && file.isEmpty()) {
            if (DispatcherUtils.isAsyncEnabled()) {
                DispatcherUtils.dispatchGetPackageVersions(request, response, context, id)
            } else {
                getVersions(context, response, id)
            }
        } else if (file == "/$id.$version.") {
            if (DispatcherUtils.isAsyncEnabled()) {
                DispatcherUtils.dispatchGetPackageContent(request, response, context, id, version, extension)
            } else {
                getContent(context, response, id, version, extension)
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
        }
    }

    private fun getContent(context: JsonNuGetFeedContext,
                           response: HttpServletResponse,
                           id: String,
                           version: String,
                           extension: String) {

        val packageSource = packageSourceFactory.create(context.feed)
        val packages = packageSource.getPackages(id, version)
        val adapter = adapterFactory.create(context)

        if (packages.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id:$version not found")
            return
        }

        var redirectUrl = adapter.createDownloadContentUrl(packages.first(), extension)
        if (redirectUrl.isNullOrEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported format $extension")
            return
        }
        response.sendRedirect(redirectUrl)
    }

    private fun getVersions(context: JsonNuGetFeedContext, response: HttpServletResponse, id: String) {
        val packageSource = packageSourceFactory.create(context.feed)
        val packages = packageSource.getPackages(id)
        val adapter = adapterFactory.create(context)

        if (packages.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id not found")
            return
        }

        response.writeJson(adapter.createPackageVersionsResponse(packages))
    }

    companion object {
        private val FLAT_CONTAINER_URL = Regex("\\/flatcontainer\\/([^\\/]+)\\/([^\\/]+)((\\/[^\\/]+\\.)(nupkg|nuspec))?")
    }
}
