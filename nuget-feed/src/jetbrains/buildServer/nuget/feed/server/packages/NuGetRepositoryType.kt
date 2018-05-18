package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.common.index.PackageConstants

class NuGetRepositoryType(repositoryRegistry: RepositoryRegistry) : RepositoryType {

    override val type: String
        get() = PackageConstants.NUGET_PROVIDER_ID

    init {
        repositoryRegistry.register(this)
    }

    override fun createRepository(parameters: Map<String, String>): Repository {
        return NuGetRepository(parameters)
    }
}
