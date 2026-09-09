package jetbrains.buildServer.nuget.feed.server.impl

import jetbrains.buildServer.ProjectAwareRootUrlResolver
import javax.servlet.http.HttpServletRequest

class NuGetFeedRootUrlResolver(private val myRootUrlResolver: ProjectAwareRootUrlResolver) {

    /**
     * The URL a NuGet client should use for a feed: the URL configured for the feed's project, or the URL the client is already talking to
     * when the project redefines none.
     */
    fun getRootUrl(request: HttpServletRequest, projectExternalId: String?): String {
        val configuredRootUrl = getRootUrl(projectExternalId)
        return if (configuredRootUrl != globalServerUrl) {
            // Project redefines the URL
            configuredRootUrl
        } else {
            // Keep the client on the origin it actually reached as the global URL might still be default
            withoutTrailingSlash(HttpServletRequestUtil.getRootUrl(request))
        }
    }

    fun getRootUrlWithAuthenticationType(request: HttpServletRequest, projectExternalId: String?): String =
        getRootUrl(request, projectExternalId) + HttpServletRequestUtil.getAuthenticationTypePath(request)

    fun getRootUrl(projectExternalId: String?): String =
        withoutTrailingSlash(myRootUrlResolver.getRootUrlByProjectExternalId(projectExternalId))

    private val globalServerUrl: String get() = withoutTrailingSlash(myRootUrlResolver.rootUrl)

    private fun withoutTrailingSlash(rootUrl: String) = rootUrl.removeSuffix("/")
}
