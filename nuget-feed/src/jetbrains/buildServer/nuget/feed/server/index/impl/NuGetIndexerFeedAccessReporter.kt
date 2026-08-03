package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.serverSide.BuildServerAdapter
import jetbrains.buildServer.serverSide.BuildServerListener
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.util.EventDispatcher

class NuGetIndexerFeedAccessReporter(
    private val myFeedsProvider: NuGetBuildFeedsProvider,
    private val mySettings: NuGetServerSettings,
    dispatcher: EventDispatcher<BuildServerListener>
) : BuildServerAdapter() {

    init {
        dispatcher.addListener(this)
    }

    override fun buildStarted(build: SRunningBuild) {
        if (shouldCheckAccessibleFeeds(build)) {
            return
        }

        myFeedsProvider.resolveIndexerFeeds(build).rejected.forEach {
            val problemId = it.hashCode().toUInt().toString(36)
            build.addBuildProblem(
                BuildProblemData.createBuildProblem(
                    problemId,
                    "nugetFeedNotAccessible",
                    "NuGet indexer targets feed '$it', which is no longer accessible to this build's project. It may " +
                            "have been deleted. Packages will not be indexed into it."
                )
            )
        }
    }


    private fun shouldCheckAccessibleFeeds(build: SRunningBuild): Boolean {
        return mySettings.isNuGetServerEnabled && NuGetIndexUtils.isIndexingEnabledForBuild(build)
    }
}
