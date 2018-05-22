package jetbrains.buildServer.nuget.feed.server.index

import jetbrains.buildServer.nuget.common.index.PackageConstants
import java.util.*

class NuGetFeedData(val projectId: String, val feedId: String) {
    val key: String by lazy {
        return@lazy if (projectId == "_Root" && feedId == "global") {
            PackageConstants.NUGET_PROVIDER_ID
        } else {
            "${PackageConstants.NUGET_PROVIDER_ID}.$projectId.$feedId"
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(projectId, feedId)
    }

    override fun toString(): String {
        return "$projectId/$feedId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NuGetFeedData

        if (projectId != other.projectId) return false
        if (feedId != other.feedId) return false

        return true
    }

    companion object {
        private const val GLOBAL_PROJECT_ID = "_Root"
        private const val GLOBAL_FEED_ID = "global"
        @JvmField
        val GLOBAL = NuGetFeedData(GLOBAL_PROJECT_ID, GLOBAL_FEED_ID)
    }
}
