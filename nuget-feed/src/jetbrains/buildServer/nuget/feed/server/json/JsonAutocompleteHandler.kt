package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonAutocompleteHandler(private val feedFactory: NuGetFeedFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val nuGetFeed = feedFactory.createFeed(feedData)
        val query = request.getParameter("q")
        val id = request.getParameter("id")
        val skip = request.getParameter("skip")?.toIntOrNull()
        val take = request.getParameter("take")?.toIntOrNull() ?: NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE
        val prerelease = request.getParameter("prerelease")?.toBoolean() ?: false
        val includeSemVer2 = request.includeSemVer2()

        if (!query.isNullOrEmpty()) {
            autocompleteNames(nuGetFeed, response, query, skip, take, prerelease, includeSemVer2)
            return
        }

        if (!id.isNullOrEmpty()) {
            autocompleteVersions(nuGetFeed, response, id, prerelease, includeSemVer2)
            return
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "id or q query parameters should be defined.")
    }

    private fun autocompleteNames(feed: NuGetFeed,
                                  response: HttpServletResponse,
                                  id: String,
                                  skip: Int?,
                                  take: Int,
                                  prerelease: Boolean,
                                  includeSemVer2: Boolean) {
        val query = mutableMapOf(NuGetPackageAttributes.ID to id)
        if (!prerelease) {
            query[NuGetPackageAttributes.IS_PRERELEASE] = false.toString()
        }

        val results = feed.find(query, includeSemVer2).groupBy { it.packageInfo.id }
        val totalHits = results.size

        var keys = results.keys.asSequence()
        skip?.let {
            keys = keys.drop(it)
        }

        response.writeJson(JsonAutocompleteNames(totalHits, keys.take(take).toList()))
    }

    private fun autocompleteVersions(feed: NuGetFeed,
                                     response: HttpServletResponse,
                                     id: String,
                                     prerelease: Boolean,
                                     includeSemVer2: Boolean) {
        val query = mutableMapOf(NuGetPackageAttributes.ID to id)
        if (!prerelease) {
            query[NuGetPackageAttributes.IS_PRERELEASE] = true.toString()
        }

        val results = feed.find(query, includeSemVer2).filter {
            it.packageInfo.id == id
        }

        val versions = results.map { it.version.toString() }
        response.writeJson(JsonAutocompleteVersions(versions))
    }
}
