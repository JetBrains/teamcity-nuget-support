package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry
import jetbrains.buildServer.serverSide.packages.RepositoryUsagesProvider

class NuGetRepositoryUsagesProvider(private val myProjectManager: ProjectManager,
                                    private val myStorage: MetadataStorage,
                                    registry: RepositoryRegistry) : RepositoryUsagesProvider {

    init {
        registry.register(this)
    }

    override fun getType(): String {
        return PackageConstants.NUGET_PROVIDER_ID
    }

    override fun getUsagesCount(repository: Repository): Long {
        val nuGetRepository = repository as? NuGetRepository ?: return 0
        val project = myProjectManager.findProjectById(nuGetRepository.projectId) ?: return 0
        val feedData = NuGetFeedData(project.projectId, project.externalId, nuGetRepository.name)
        return myStorage.getNumberOfEntries(feedData.key).toLong()
    }
}
