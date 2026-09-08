package jetbrains.buildServer.nuget.feed.server.impl

import jetbrains.buildServer.ProjectAwareRootUrlResolver
import javax.servlet.http.HttpServletRequest

class NuGetFeedRootUrlResolver(private val myRootUrlResolver: ProjectAwareRootUrlResolver) {

    fun getRootUrl(request: HttpServletRequest, projectExternalId: String?): String {
        val projectRootUrl = myRootUrlResolver.getRootUrlByProjectExternalId(projectExternalId)
        val rootUrl = if (projectRootUrl == myRootUrlResolver.rootUrl) HttpServletRequestUtil.getRootUrl(request) else projectRootUrl
        return rootUrl.removeSuffix("/")
    }

    fun getRootUrlWithAuthenticationType(request: HttpServletRequest, projectExternalId: String?): String =
        getRootUrl(request, projectExternalId) + HttpServletRequestUtil.getAuthenticationTypePath(request)
}
