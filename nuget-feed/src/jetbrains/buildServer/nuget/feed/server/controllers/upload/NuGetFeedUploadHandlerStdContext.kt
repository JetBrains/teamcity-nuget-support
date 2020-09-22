package jetbrains.buildServer.nuget.feed.server.controllers.upload

import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandlerContext
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData

public interface NuGetFeedUploadHandlerStdContext : NuGetFeedUploadHandlerContext {
    val feedData: NuGetFeedData
}

