package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import javax.servlet.http.HttpServletRequest

class JsonNuGetFeedContext(val feed: NuGetFeed, request: HttpServletRequest) {
    val rootUrl: String
    val rootWithAuthenticationTypeUrl: String
    val servletPath: String
    val pathInfo: String

    init {
        rootUrl = request.getRootUrl()
        rootWithAuthenticationTypeUrl = request.getRootUrlWithAuthenticationType()
        servletPath = request.servletPath
        pathInfo = request.pathInfo
    }

    fun getDownloadUrl(entry : NuGetIndexEntry): String {
        return "${rootWithAuthenticationTypeUrl}${entry.packageDownloadUrl}"
    }

    fun getDownloadNuspecUrl(entry: NuGetIndexEntry): String {
        return getDownloadUrl(entry) + "!/${entry.attributes[NuGetPackageAttributes.ID]}.nuspec"
    }

    fun getFullPath(): String {
        return "$rootUrl$servletPath$pathInfo"
    }

    fun getRegistrationUrl(id: String, version : String): String {
        return "$rootUrl$servletPath/registration1/$id/$version.json"
    }

    fun getRegistrations(id: String): String {
        return "$rootUrl$servletPath/registration1/$id/index.json"
    }
}
