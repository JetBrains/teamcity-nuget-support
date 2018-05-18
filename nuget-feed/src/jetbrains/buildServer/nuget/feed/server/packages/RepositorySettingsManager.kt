package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor

interface RepositorySettingsManager {

    fun addRepository(project: SProject, repository: Repository): SProjectFeatureDescriptor

    fun removeRepository(project: SProject, repositoryId: String)

    fun getRepositories(project: SProject, includeParent: Boolean): Collection<SProjectFeatureDescriptor>
}
