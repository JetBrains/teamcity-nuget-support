package jetbrains.buildServer.nuget.feed.server.impl

import jetbrains.buildServer.ProjectAwareRootUrlResolver
import javax.servlet.http.HttpServletRequest

class NuGetFeedRootUrlResolver(private val myRootUrlResolver: ProjectAwareRootUrlResolver) {

    /**
     * NuGet clients may reach the server via a host which differs from the configured root URL,
     * so the URL derived from the request is kept unless the project redefines the root URL.
     */
    fun getRootUrl(request: HttpServletRequest, projectExternalId: String?): String {
        val projectRootUrl = myRootUrlResolver.getRootUrlByProjectExternalId(projectExternalId)
        val rootUrl = if (projectRootUrl == myRootUrlResolver.rootUrl) HttpServletRequestUtil.getRootUrl(request) else projectRootUrl
        // a project may define the root URL with a trailing slash, while callers append absolute paths
        return rootUrl.removeSuffix("/")
    }

    fun getRootUrlWithAuthenticationType(request: HttpServletRequest, projectExternalId: String?): String =
        getRootUrl(request, projectExternalId) + HttpServletRequestUtil.getAuthenticationTypePath(request)
}
