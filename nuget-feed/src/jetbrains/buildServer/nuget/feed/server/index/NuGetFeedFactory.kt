package jetbrains.buildServer.nuget.feed.server.index

interface NuGetFeedFactory {
    fun createFeed(feedData: NuGetFeedData): NuGetFeed
}
