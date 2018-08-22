package jetbrains.buildServer.nuget.feed.server.odata4j

import jetbrains.buildServer.nuget.common.version.SemanticVersion
import jetbrains.buildServer.nuget.feed.server.MetadataConstants
import org.odata4j.producer.QueryInfo
import java.util.*

private val VERSION_20 = Objects.requireNonNull<SemanticVersion>(SemanticVersion.valueOf("2.0.0"))

object ODataUtilities {
    @JvmStatic
    fun includeSemVer2(queryInfo: QueryInfo?): Boolean {
        if (queryInfo != null) {
            val queryParams = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
            queryParams.putAll(queryInfo.customOptions)

            val semVerLevel = queryParams[MetadataConstants.SEMANTIC_VERSION]
            if (semVerLevel != null) {
                val version = SemanticVersion.valueOf(semVerLevel)
                return version != null && version >= VERSION_20
            }
        }
        return false
    }
}

