package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.RepositoryType

class NuGetRepository(type: RepositoryType, projectId: String, parameters: Map<String, String>)
    : Repository(type, projectId, parameters) {

    override fun getUrlPaths() = listOf(NuGetUtils.getProjectFeedPath(projectId, name))
}
