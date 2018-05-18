package jetbrains.buildServer.nuget.feed.server.packages

class RepositoryRegistryImpl : RepositoryRegistry {
    private val types = hashMapOf<String, RepositoryType>()

    override fun register(repositoryType: RepositoryType) {
        types[repositoryType.type] = repositoryType
    }

    override fun findType(type: String): RepositoryType? {
        return types[type]
    }
}
