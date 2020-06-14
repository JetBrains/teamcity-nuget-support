package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.common.version.VersionUtility
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonRegistrationHandler(private val feedFactory: NuGetFeedFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val matchResult = REGISTRATION_URL.find(request.pathInfo)
        if (matchResult != null) {
            val (id, resource) = matchResult.destructured
            val feed = feedFactory.createFeed(feedData)
            if (resource == "index") {
                getAllRegistrations(feed, request, response, id)
            } else {
                getRegistration(feed, request, response, id, resource)
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
        }
    }

    private fun getRegistration(feed: NuGetFeed, request: HttpServletRequest, response: HttpServletResponse, id: String, version: String) {
        val results = feed.find(mapOf(
                NuGetPackageAttributes.ID to id,
                NuGetPackageAttributes.VERSION to version
        ), true)

        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id:$version not found")
            return
        }

        val entry = results.first()
        val rootUrl = request.getRootUrl()
        val downloadUrl = "${request.getRootUrlWithAuthenticationType()}${entry.packageDownloadUrl}"
        val packageResponse = entry.toRegistrationEntry(
                "$rootUrl${request.servletPath}${request.pathInfo}",
                listOf("Package", "catalog:Permalink"),
                downloadUrl
        )

        response.writeJson(packageResponse)
    }

    private fun getAllRegistrations(feed: NuGetFeed, request: HttpServletRequest, response: HttpServletResponse, id: String) {
        val results = feed.findPackagesById(id, true)
        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id not found")
            return
        }

        val rootUrl = request.getRootUrl()
        val rootUrlWithAuthenticationType = request.getRootUrlWithAuthenticationType()
        val items = results.map {
            val version = VersionUtility.normalizeVersion(it.version)
            val registrationUrl = "$rootUrl${request.servletPath}/registration1/$id/$version.json"
            val downloadUrl = "$rootUrlWithAuthenticationType${it.packageDownloadUrl}"
            JsonRegistrationPackage(
                    registrationUrl,
                    "Package",
                    it.toRegistrationEntry(
                            registrationUrl,
                            listOf("PackageDetails"),
                            registrationUrl
                    ),
                    downloadUrl,
                    registrationUrl
            )
        }
        val registrationPage = JsonRegistrationPage(
                "$rootUrl${request.servletPath}${request.pathInfo}",
                results.size,
                lower = VersionUtility.normalizeVersion(results.first().version),
                upper = VersionUtility.normalizeVersion(results.last().version),
                items = items
        )
        val registration = JsonRegistrationResponse(
                "$rootUrl${request.servletPath}${request.pathInfo}",
                listOf("catalog:CatalogRoot", "PackageRegistration", "catalog:Permalink"),
                1,
                listOf(registrationPage)
        )

        response.writeJson(registration)
    }

    companion object {
        private val REGISTRATION_URL = Regex("\\/registration1\\/([^\\/]+)\\/([^\\/]+)\\.json")
    }
}
