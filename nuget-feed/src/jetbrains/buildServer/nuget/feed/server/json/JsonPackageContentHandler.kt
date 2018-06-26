package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.web.util.WebUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonPackageContentHandler(private val feedFactory: NuGetFeedFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val matchResult = FLAT_CONTAINER_URL.find(request.pathInfo)
        if (matchResult == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
            return
        }

        val (id, version, file) = matchResult.destructured
        val feed = feedFactory.createFeed(feedData)
        if (version == "index.json" && file.isEmpty()) {
            getVersions(feed, response, id)
        } else if (file == "/$id.$version.nupkg") {
            getContent(feed, request, response, id, version)
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
            return
        }
    }

    private fun getContent(feed: NuGetFeed, request: HttpServletRequest, response: HttpServletResponse, id: String, version: String) {
        val results = feed.find(mapOf(
                NuGetPackageAttributes.ID to id,
                NuGetPackageAttributes.VERSION to version
        ))

        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id:$version not found")
            return
        }

        val entry = results.first()
        val rootUrl = WebUtil.getRootUrl(request)
        response.sendRedirect("$rootUrl${entry.attributes[PackageConstants.TEAMCITY_DOWNLOAD_URL]}")
    }

    private fun getVersions(feed: NuGetFeed, response: HttpServletResponse, id: String) {
        val results = feed.findPackagesById(id)
        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id not found")
            return
        }

        val versions = results.map { it.getVersion() }
        response.writeJson(JsonPackageVersions(versions))
    }

    companion object {
        private val FLAT_CONTAINER_URL = Regex("\\/flatcontainer\\/([^\\/]+)\\/([^\\/]+)(\\/[^\\/]+\\.nupkg)?")
    }
}
