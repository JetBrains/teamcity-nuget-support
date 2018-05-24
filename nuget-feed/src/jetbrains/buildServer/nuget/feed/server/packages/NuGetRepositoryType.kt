package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.web.openapi.PluginDescriptor

class NuGetRepositoryType(repositoryRegistry: RepositoryRegistry,
                          pluginDescriptor: PluginDescriptor) : RepositoryType() {

    private val editParametersUrl = pluginDescriptor.getPluginResourcesPath("editNuGetRepository.html")

    init {
        repositoryRegistry.register(this)
    }

    override fun getType() = PackageConstants.NUGET_PROVIDER_ID

    override fun getName() = "NuGet Feed"

    override fun getEditParametersUrl() = editParametersUrl

    override fun createRepository(project: SProject, parameters: Map<String, String>): Repository {
        return NuGetRepository(this, project, parameters.toMutableMap())
    }
}
