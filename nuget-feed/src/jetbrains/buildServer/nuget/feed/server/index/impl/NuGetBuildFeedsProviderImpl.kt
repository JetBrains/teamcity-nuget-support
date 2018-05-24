package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager

class NuGetBuildFeedsProviderImpl(private val myProjectManager: ProjectManager,
                                  private val myRepositoryManager: RepositoryManager) : NuGetBuildFeedsProvider {

    override fun getFeeds(build: SBuild): Set<NuGetFeedData> {
        val nugetFeeds = hashSetOf<NuGetFeedData>()

        // Add projects with enabled NuGet feed indexing
        val buildProject = myProjectManager.findProjectById(build.projectId)
        NuGetIndexUtils.findFeedsWithIndexing(buildProject, myRepositoryManager).forEach {
            nugetFeeds.add(NuGetFeedData(it.projectId, it.name))
        }

        // Add projects from NuGet Package Indexer build features
        build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach { feature ->
            feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let {
                NuGetUtils.feedIdToData(it)?.let {
                    val project = myProjectManager.findProjectByExternalId(it.first)
                    if (project != null && myRepositoryManager.hasRepository(project, PackageConstants.NUGET_PROVIDER_ID, it.second)) {
                        nugetFeeds.add(NuGetFeedData(project.projectId, it.second))
                    } else {
                        LOG.warn("Could not find '${it.second}' NuGet feed for '${it.first}' project.")
                    }
                }
            }
        }

        return nugetFeeds
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetBuildFeedsProviderImpl::class.java.name)
    }
}
