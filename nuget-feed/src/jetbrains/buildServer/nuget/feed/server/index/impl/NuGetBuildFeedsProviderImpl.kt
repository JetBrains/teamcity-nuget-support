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

        val buildProject = myProjectManager.findProjectById(build.projectId)
        NuGetIndexUtils.findFeedsWithIndexing(buildProject, myRepositoryManager).forEach {
            nugetFeeds.add(NuGetFeedData(it.projectId, it.name))
        }

        try {
            build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach { feature ->
                feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let { feedId ->
                    NuGetUtils.feedIdToData(feedId)?.let { (feedProjectExtId, feedName) ->
                        myProjectManager.findProjectByExternalId(feedProjectExtId)?.let { feedProject ->
                            val feed = NuGetFeedData(feedProject.projectId, feedName)
                            if (hasWritePermissionsToFeed(build, feed)) {
                                nugetFeeds.add(feed)
                            } else {
                                LOG.debug("Build #${build.buildId} (project '${build.projectId}') requested NuGet indexing " +
                                    "into feed '$feedProjectExtId/$feedName', which is not visible to the build's project; skipping.")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOG.warnAndDebugDetails("Unable to get list of build #${build.buildId} features", e)
        }

        return nugetFeeds
    }

    override fun hasWritePermissionsToFeed(build: SBuild, feed: NuGetFeedData): Boolean {
        val buildProject = myProjectManager.findProjectById(build.projectId) ?: return false
        return myRepositoryManager.getRepositories(buildProject, true)
            .filterIsInstance<NuGetRepository>()
            .any { it.projectId == feed.projectId && it.name == feed.feedId }
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetBuildFeedsProviderImpl::class.java.name)
    }
}
