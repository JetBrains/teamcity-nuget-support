package jetbrains.buildServer.nuget.feed.server

import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.browser.Browser

class NuGetFeedUrlsProvider(private val myProjectManager: ProjectManager,
                            private val myRepositoryManager: RepositoryManager,
                            private val myLoginConfiguration: LoginConfiguration) : ProjectDataFetcher {

    override fun getType() = "NuGetFeedUrls"

    override fun retrieveData(browser: Browser, queryString: String): MutableList<DataItem> {
        val parameters = getParameters(queryString)
        val project = getProject(parameters) ?: return mutableListOf()
        val authTypes = getAuthTypes(parameters)
        val apiVersions = getApiVersions(parameters)

        return myRepositoryManager.getRepositories(project, true)
                .filterIsInstance<NuGetRepository>()
                .flatMap { repository ->
                    myProjectManager.findProjectById(repository.projectId)?.let { project ->
                        apiVersions.flatMap { version ->
                            authTypes.map { authType ->
                                val feedReference = NuGetUtils.getProjectFeedReference(
                                        authType, project.externalId, repository.name, version
                                )
                                DataItem(ReferencesResolverUtil.makeReference(feedReference), null)
                            }
                        }
                    } ?: emptyList()
                }.toMutableList()
    }

    private fun getApiVersions(parameters: Map<String, String>): Set<NuGetAPIVersion> {
        parameters["apiVersions"]?.let { apiVersions ->
            val versions = apiVersions.split(";").toSet()
            return NuGetAPIVersion.values().filter {
                versions.contains(it.name.toLowerCase())
            }.toSet()
        }

        return NuGetAPIVersion.values().toSet()
    }

    private fun getAuthTypes(parameters: Map<String, String>): Set<String> {
        parameters["authTypes"]?.let { authTypes ->
            val types = authTypes.split(";").toMutableSet()
            if (types.contains(GUEST_AUTH) && !myLoginConfiguration.isGuestLoginAllowed) {
                types.remove(GUEST_AUTH)
            }
            return types
        }
        return setOf(HTTP_AUTH)
    }

    private fun getProject(parameters: Map<String, String>): SProject? {
        parameters["buildType"]?.let { buildType ->
            return myProjectManager.findBuildTypeByExternalId(buildType)?.project
        }
        parameters["template"]?.let { template ->
            return myProjectManager.findBuildTypeTemplateByExternalId(template)?.project
        }
        return null
    }

    private fun getParameters(queryString: String): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        queryString.split("&").forEach { pair ->
            val segments = pair.split("=")
            if (segments.size != 2 || segments.any { it.isEmpty() }) {
                return@forEach
            }
            parameters[segments[0]] = segments[1]
        }
        return parameters
    }

    companion object {
        const val GUEST_AUTH = "guestAuth"
        const val HTTP_AUTH = "httpAuth"
    }
}
