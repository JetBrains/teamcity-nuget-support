package jetbrains.buildServer.nuget.feed.server

import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.browser.Browser

class NuGetFeedUrlsProvider(private val myProjectManager: ProjectManager,
                            private val myRepositoryManager: RepositoryManager,
                            private val myLoginConfiguration: LoginConfiguration) : ProjectDataFetcher {

    override fun getType() = "NuGetFeedUrls"

    override fun retrieveData(browser: Browser, buildTypeId: String): MutableList<DataItem> {
        val segments = buildTypeId.split(":")
        val buildTypeByExternalId = myProjectManager.findBuildTypeByExternalId(segments.first())
                ?: return mutableListOf()

        val authType = if (GUEST_AUTH == segments.last() && myLoginConfiguration.isGuestLoginAllowed) {
            GUEST_AUTH
        } else {
            HTTP_AUTH
        }

        return myRepositoryManager.getRepositories(buildTypeByExternalId.project, true)
                .filterIsInstance<NuGetRepository>()
                .flatMap { repository ->
                    myProjectManager.findProjectById(repository.projectId)?.let { project ->
                        NuGetAPIVersion.values().map { version ->
                            val feedReference = NuGetUtils.getProjectFeedReference(
                                    authType, project.externalId, repository.name, version
                            )
                            DataItem(ReferencesResolverUtil.makeReference(feedReference), null)
                        }
                    } ?: emptyList()
                }.toMutableList()
    }

    companion object {
        const val GUEST_AUTH = "guestAuth"
        const val HTTP_AUTH = "httpAuth"
    }
}
