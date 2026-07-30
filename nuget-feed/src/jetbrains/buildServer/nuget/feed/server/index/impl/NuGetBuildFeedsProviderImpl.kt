package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.nuget.feed.server.index.impl.security.IndexerFeedsResolutionResult
import jetbrains.buildServer.nuget.feed.server.index.impl.security.NuGetFeedPermissionChecker
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager

class NuGetBuildFeedsProviderImpl(private val myProjectManager: ProjectManager,
                                  private val myRepositoryManager: RepositoryManager,
                                  private val myPermissionChecker: NuGetFeedPermissionChecker
) : NuGetBuildFeedsProvider {

    override fun getFeeds(build: SBuild): Set<NuGetFeedData> {
        val nugetFeeds = hashSetOf<NuGetFeedData>()
        nugetFeeds.addAll(resolveIndexerFeeds(build).accessible)
        return nugetFeeds
    }

    override fun resolveIndexerFeeds(build: SBuild): IndexerFeedsResolutionResult {
        val buildProject = myProjectManager.findProjectById(build.projectId)
        val accessible = hashSetOf<NuGetFeedData>()
        val rejectedIds = arrayListOf<String>()

        val writableFeeds = buildProject?.let { myPermissionChecker.getWritableFeeds(it) } ?: emptySet()

        NuGetIndexUtils.findFeedsWithIndexing(buildProject, myRepositoryManager).forEach {
            val feed = NuGetFeedData(it.projectId, it.name)
            if (writableFeeds.contains(feed)) {
                accessible.add(feed)
            } else {
                rejectedIds.add(feed.feedId)
            }
        }

        try {
            build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach { feature ->
                feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let { feedId ->
                    NuGetUtils.feedIdToData(feedId)?.let { (feedProjectExtId, feedName) ->
                        val feed = myProjectManager.findProjectByExternalId(feedProjectExtId)?.let {
                            NuGetFeedData(it.projectId, feedName)
                        }
                        if (feed != null && writableFeeds.contains(feed)) {
                            accessible.add(feed)
                        } else {
                            rejectedIds.add(feedId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOG.warnAndDebugDetails("Unable to get list of build #${build.buildId} features", e)
        }
        return IndexerFeedsResolutionResult(accessible, rejectedIds)
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetBuildFeedsProviderImpl::class.java.name)
    }
}
