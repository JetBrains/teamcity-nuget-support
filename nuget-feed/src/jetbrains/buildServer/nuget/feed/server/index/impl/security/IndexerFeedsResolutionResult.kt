package jetbrains.buildServer.nuget.feed.server.index.impl.security

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData

data class IndexerFeedsResolutionResult(
    val accessible: Set<NuGetFeedData>,
    val rejected: List<String>
)
