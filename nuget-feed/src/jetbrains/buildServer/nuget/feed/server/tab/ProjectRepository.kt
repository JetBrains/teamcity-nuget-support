package jetbrains.buildServer.nuget.feed.server.tab

import jetbrains.buildServer.nuget.feed.server.packages.Repository
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.util.WebUtil
import java.net.URI
import javax.ws.rs.core.UriBuilder

class ProjectRepository(val repository: Repository, val project: SProject, rootUrl: String) {
    private val uriBuilder = UriBuilder.fromUri(rootUrl)

    val httpAuthUrls: List<URI>
        get() = repository.urlPaths.map {
            uriBuilder.replacePath(WebUtil.combineContextPath(WebUtil.HTTP_AUTH_PREFIX, it)).build()
        }

    val guestAuthUrls: List<URI>
        get() = repository.urlPaths.map {
            uriBuilder.replacePath(WebUtil.combineContextPath(WebUtil.GUEST_AUTH_PREFIX, it)).build()
        }
}
