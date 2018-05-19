package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.feed.server.NuGetUtils

class NuGetRepository(override val parameters: Map<String, String>) : Repository(parameters) {

    override val urlPaths: List<String>
        get() = listOf(NuGetUtils.getProjectNuGetFeedPath(name))
}
