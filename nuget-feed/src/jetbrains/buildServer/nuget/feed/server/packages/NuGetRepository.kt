package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.RepositoryType

class NuGetRepository(type: RepositoryType, private val project: SProject, parameters: Map<String, String>)
    : Repository(type, project.projectId, parameters) {

    override fun getUrlPaths() = listOf(NuGetUtils.getProjectFeedPath(project.externalId, name))

    var indexPackages: Boolean
        get() {
            return parameters[INDEX_PACKAGES]?.toBoolean() ?: false
        }
        set(value) {
            parameters[INDEX_PACKAGES] = value.toString()
        }

    companion object {
        const val INDEX_PACKAGES = "indexPackages"
    }
}
