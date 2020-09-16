package jetbrains.buildServer.nuget.feed.server.controllers.upload

import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandlerStdContext
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData

public class NuGetFeedUploadHandlerStdContextImpl(
        override val feedData: NuGetFeedData
) : NuGetFeedUploadHandlerStdContext {
    override fun getFeedName(): String {
        return feedData.toString();
    }
}
