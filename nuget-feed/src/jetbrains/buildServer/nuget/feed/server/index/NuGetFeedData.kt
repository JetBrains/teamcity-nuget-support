package jetbrains.buildServer.nuget.feed.server.index

import jetbrains.buildServer.nuget.common.index.PackageConstants

class NuGetFeedData(val projectId: String, val feedId: String) {
    val key: String by lazy {
        return@lazy if (projectId == "_Root" && feedId == "global") {
            PackageConstants.NUGET_PROVIDER_ID
        } else {
            "${PackageConstants.NUGET_PROVIDER_ID}.$projectId.$feedId"
        }
    }

    companion object {
        private const val GLOBAL_PROJECT_ID = "_Root"
        private const val GLOBAL_FEED_ID = "global"
        @JvmField
        val GLOBAL = NuGetFeedData(GLOBAL_PROJECT_ID, GLOBAL_FEED_ID)
    }
}
