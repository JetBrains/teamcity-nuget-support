package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.SBuild

interface NuGetBuildFeedsProvider {
    fun getFeeds(build: SBuild): Set<NuGetFeedData>
}
