package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor

class RepositorySettingsManagerImpl : RepositorySettingsManager {

    override fun addRepository(project: SProject, repository: Repository): SProjectFeatureDescriptor {
        return project.addFeature(RepositoryConstants.PACKAGES_FEATURE_TYPE, repository.parameters)
    }

    override fun removeRepository(project: SProject, repositoryId: String) {
        val descriptor = project.findFeatureById(repositoryId)
            ?: throw IllegalArgumentException("Package packages with id $repositoryId is not found")
        project.removeFeature(descriptor.id)
    }

    override fun getRepositories(project: SProject, includeParent: Boolean): Collection<SProjectFeatureDescriptor> {
        return if (includeParent) {
            project.getAvailableFeaturesOfType(RepositoryConstants.PACKAGES_FEATURE_TYPE)
        } else {
            project.getOwnFeaturesOfType(RepositoryConstants.PACKAGES_FEATURE_TYPE)
        }
    }
}
