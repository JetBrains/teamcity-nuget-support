package jetbrains.buildServer.nuget.feed.server.packages

interface RepositoryType {
    val type: String
    fun createRepository(parameters: Map<String, String>): Repository
}
