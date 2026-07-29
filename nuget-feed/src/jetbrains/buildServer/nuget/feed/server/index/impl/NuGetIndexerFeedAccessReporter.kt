package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.BuildServerAdapter
import jetbrains.buildServer.serverSide.BuildServerListener
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.util.EventDispatcher

class NuGetIndexerFeedAccessReporter(
    private val myProjectManager: ProjectManager,
    private val myFeedsProvider: NuGetBuildFeedsProvider,
    dispatcher: EventDispatcher<BuildServerListener>
) : BuildServerAdapter() {

    init {
        dispatcher.addListener(this)
    }

    override fun buildStarted(build: SRunningBuild) {
        build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach { feature ->
            feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let { feedId ->
                NuGetUtils.feedIdToData(feedId)?.let { (feedProjectExtId, feedName) ->
                    val feed = myProjectManager.findProjectByExternalId(feedProjectExtId)?.let { NuGetFeedData(it.projectId, feedName) }
                    if (feed == null || !myFeedsProvider.hasWritePermissionsToFeed(build, feed)) {
                        build.addBuildProblem(BuildProblemData.createBuildProblem(
                            "nugetFeedNotAccessible",
                            "nugetFeedNotAccessible",
                            "NuGet indexer targets feed '$feedProjectExtId/$feedName', which is no longer accessible to this build's project, it may have been " +
                                    "deleted. Packages will not be indexed into it."))
                    }
                }
            }
        }
    }
}
