package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.security.IndexerFeedsResolutionResult
import jetbrains.buildServer.serverSide.SBuild

interface NuGetBuildFeedsProvider {
    /**
     * Provides NuGet feeds accessible to the given build
     */
    fun getFeeds(build: SBuild): Set<NuGetFeedData>

    /**
     * @returns NuGet fields resolvable (not necessarily accessible) by the given build
     */
    fun resolveIndexerFeeds(build: SBuild): IndexerFeedsResolutionResult
}
