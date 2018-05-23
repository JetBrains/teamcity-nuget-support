package jetbrains.buildServer.nuget.feed.server.index

import jetbrains.buildServer.nuget.common.index.PackageConstants
import java.util.*

class NuGetFeedData(val projectId: String, val feedId: String) {
    val key: String by lazy {
        return@lazy if (projectId == DEFAULT_PROJECT_ID && feedId == DEFAULT_FEED_ID) {
            PackageConstants.NUGET_PROVIDER_ID
        } else {
            "${PackageConstants.NUGET_PROVIDER_ID}.$projectId.$feedId"
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(projectId, feedId)
    }

    override fun toString(): String {
        return if (feedId == DEFAULT_FEED_ID) projectId else "$projectId/$feedId"
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
        private const val DEFAULT_PROJECT_ID = "_Root"
        const val DEFAULT_FEED_ID = "default"
        @JvmField
        val DEFAULT = NuGetFeedData(DEFAULT_PROJECT_ID, DEFAULT_FEED_ID)
    }
}
