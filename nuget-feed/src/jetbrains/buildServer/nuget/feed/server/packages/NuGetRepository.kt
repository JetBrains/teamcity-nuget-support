package jetbrains.buildServer.nuget.feed.server.packages

class NuGetRepository(override val parameters: Map<String, String>) : Repository(parameters) {

    override val urlPaths: List<String>
        get() = listOf("/app/nuget/$name/v2")
}
