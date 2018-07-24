package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.web.util.WebUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonSearchQueryHandler(private val feedFactory: NuGetFeedFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val nuGetFeed = feedFactory.createFeed(feedData)
        val rootUrl = WebUtil.getRootUrl(request)
        val query = request.getParameter("q")
        val skip = request.getParameter("skip")?.toIntOrNull()
        val take = request.getParameter("take")?.toIntOrNull() ?: NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE
        val prerelease = request.getParameter("prerelease")?.toBoolean() ?: false
        val semVerLevel = request.getParameter("semVerLevel")

        val results = (if (query.isNotEmpty()) {
            nuGetFeed.search(query, "", prerelease)
        } else {
            nuGetFeed.all
        }).groupBy { it.packageInfo.id }

        val totalHits = results.size
        val data = arrayListOf<JsonPackage>()
        var keys = results.keys.asSequence()
        skip?.let {
            keys = keys.drop(it)
        }

        keys.take(take).forEach {
            results[it]?.let { packages ->
                val entry = packages.first()
                val packageUrl = "$rootUrl${request.servletPath}/registration1/${it.toLowerCase()}/"
                val versions = packages.map {
                    val version = it.getVersion()
                    JsonPackageVersion(
                            "$packageUrl$version.json",
                            version,
                            0
                    )
                }
                val version = entry.getVersion()
                data.add(JsonPackage(
                        "$packageUrl$version.json",
                        "Package",
                        it,
                        version,
                        versions,
                        entry.attributes[NuGetPackageAttributes.DESCRIPTION],
                        entry.attributes[NuGetPackageAttributes.AUTHORS],
                        entry.attributes[NuGetPackageAttributes.ICON_URL],
                        entry.attributes[NuGetPackageAttributes.LICENSE_URL],
                        null,
                        entry.attributes[NuGetPackageAttributes.PROJECT_URL],
                        packageUrl + "index.json",
                        entry.attributes[NuGetPackageAttributes.SUMMARY],
                        entry.attributes[NuGetPackageAttributes.TAGS],
                        entry.attributes[NuGetPackageAttributes.TITLE],
                        null,
                        null
                ))
            }
        }

        response.writeJson(JsonSearchResponse(totalHits, data))
    }
}