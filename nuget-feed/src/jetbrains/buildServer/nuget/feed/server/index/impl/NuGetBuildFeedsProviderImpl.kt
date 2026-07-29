package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
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

        val buildProjectAccessibleFeeds = buildProject
            ?.let { myRepositoryManager.getRepositories(it, true).filterIsInstance<NuGetRepository>() }
            ?: emptyList()

        try {
            build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach { feature ->
                feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let { feedId ->
                    NuGetUtils.feedIdToData(feedId)?.let { feedData ->
                        val feedProjectExtId = feedData.first
                        val feedName = feedData.second
                        val feedProject = myProjectManager.findProjectByExternalId(feedProjectExtId)
                        val visibleToBuild = (feedProject != null && buildProjectAccessibleFeeds.any { it.projectId == feedProject.projectId && it.name == feedName })
                        if (visibleToBuild) {
                            nugetFeeds.add(NuGetFeedData(feedProject.projectId, feedName))
                        } else {
                            LOG.warn("Build #${build.buildId} (project '${build.projectId}') requested NuGet indexing " +
                                "into feed '$feedProjectExtId/$feedName', which is not visible to the build's project; " +
                                "skipping. A build may only index into feeds defined in its own project or an ancestor.")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOG.warnAndDebugDetails("Unable to get list of build #${build.buildId} features", e)
        }

        return nugetFeeds
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetBuildFeedsProviderImpl::class.java.name)
    }
}
