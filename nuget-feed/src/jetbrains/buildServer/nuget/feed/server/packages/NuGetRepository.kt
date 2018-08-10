package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.RepositoryType

class NuGetRepository(type: RepositoryType, private val project: SProject, parameters: Map<String, String>)
    : Repository(type, project.projectId, parameters) {

    override fun getParametersDescription(): String {
        return "Automatic NuGet packages indexing: ${if (indexPackages) "enabled" else "disabled"}"
    }

    override fun getUrlPaths(): List<String> = NuGetUtils.getProjectFeedPaths(project.externalId, name)

    var indexPackages: Boolean
        get() {
            return parameters[NuGetRepositoryParams.INDEX_PACKAGES]?.toBoolean() ?: false
        }
        set(value) {
            parameters[NuGetRepositoryParams.INDEX_PACKAGES] = value.toString()
        }
}
