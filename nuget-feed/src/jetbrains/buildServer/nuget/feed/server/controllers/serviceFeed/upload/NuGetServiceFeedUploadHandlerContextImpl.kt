package jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload

import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandlerContext
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload.NuGetServiceFeedUploadHandlerContext

class NuGetServiceFeedUploadHandlerContextImpl(
        private val myContext: NuGetServiceFeedHandlerContext
) : NuGetServiceFeedUploadHandlerContext {
    override fun getFeedName(): String {
        return "${myContext.projectId}/-ServiceFeed-"
    }
}
