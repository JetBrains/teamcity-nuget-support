package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.nuget.feed.server.index.impl.security.IndexerFeedsResolutionResult
import jetbrains.buildServer.nuget.feed.server.index.impl.security.NuGetFeedPermissionChecker
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager

class NuGetBuildFeedsProviderImpl(
    private val myProjectManager: ProjectManager,
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
        val isCrossProjectAccessEnabled = TeamCityProperties.getBoolean(NuGetFeedConstants.PROP_NUGET_FEED_ENABLE_CROSS_PROJECT_ACCESS)
        val isFeedAccessible: (SProject, NuGetFeedData) -> Boolean = { feedProject, feed ->
            if (isCrossProjectAccessEnabled) myRepositoryManager.hasRepository(feedProject, PackageConstants.NUGET_PROVIDER_ID, feed.feedId)
            else feed in writableFeeds
        }

        // Feeds with implicit indexing come from the build project's own hierarchy, so they are always writable.
        NuGetIndexUtils.findFeedsWithIndexing(buildProject, myRepositoryManager).forEach {
            accessible.add(NuGetFeedData(it.projectId, it.name))
        }

        try {
            build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach { feature ->
                feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let { feedId ->
                    NuGetUtils.feedIdToData(feedId)?.let { (feedProjectExtId, feedName) ->
                        val feedProject = myProjectManager.findProjectByExternalId(feedProjectExtId)
                        val feed = feedProject?.let {
                            NuGetFeedData(it.projectId, feedName)
                        }
                        if (feed != null && isFeedAccessible(feedProject, feed)) {
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
