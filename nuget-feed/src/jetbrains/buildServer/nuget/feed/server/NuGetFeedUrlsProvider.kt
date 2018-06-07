package jetbrains.buildServer.nuget.feed.server

import jetbrains.buildServer.RootUrlHolder
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.web.util.WebUtil
import javax.ws.rs.core.UriBuilder

class NuGetFeedUrlsProvider(private val myProjectManager: ProjectManager,
                            private val myRepositoryManager: RepositoryManager,
                            private val myRootUrlHolder: RootUrlHolder,
                            private val myLoginConfiguration: LoginConfiguration) : ProjectDataFetcher {

    override fun getType() = "NuGetFeedUrls"

    override fun retrieveData(browser: Browser, buildTypeId: String): MutableList<DataItem> {
        val buildTypeByExternalId = myProjectManager.findBuildTypeByExternalId(buildTypeId) ?: return mutableListOf()
        val uriBuilder = UriBuilder.fromUri(myRootUrlHolder.rootUrl)
        return myRepositoryManager.getRepositories(buildTypeByExternalId.project, true)
                .filterIsInstance<NuGetRepository>()
                .flatMap {
                    it.urlPaths.flatMap {
                        arrayListOf<String>(WebUtil.combineContextPath(WebUtil.HTTP_AUTH_PREFIX, it)).apply {
                            if (myLoginConfiguration.isGuestLoginAllowed) {
                                this.add(WebUtil.combineContextPath(WebUtil.GUEST_AUTH_PREFIX, it))
                            }
                        }
                    }
                }.map {
                    DataItem(uriBuilder.replacePath(it).build().toString(), null)
                }.toMutableList()
    }
}
