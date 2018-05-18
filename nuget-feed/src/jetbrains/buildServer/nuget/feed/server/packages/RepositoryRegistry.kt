package jetbrains.buildServer.nuget.feed.server.packages

interface RepositoryRegistry {

    fun register(repositoryType: RepositoryType)

    fun findType(type: String) : RepositoryType?
}
