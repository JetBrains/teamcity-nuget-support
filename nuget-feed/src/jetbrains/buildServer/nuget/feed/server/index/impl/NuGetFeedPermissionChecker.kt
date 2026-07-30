package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SProject

interface NuGetFeedPermissionChecker {
    fun canWrite(build: SBuild, feed: NuGetFeedData): Boolean

    fun canWrite(buildProject: SProject?, feed: NuGetFeedData): Boolean
}

data class IndexerFeedsResolutionResult(val accessible: Set<NuGetFeedData>, val rejected: List<String>)
