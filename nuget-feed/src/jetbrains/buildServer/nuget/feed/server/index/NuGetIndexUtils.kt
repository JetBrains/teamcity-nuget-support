package jetbrains.buildServer.nuget.feed.server.index

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildType
import jetbrains.buildServer.nuget.common.NuGetServerConstants
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.serverSide.impl.LogUtil

object NuGetIndexUtils {
    private val LOG = Logger.getInstance(NuGetIndexUtils::class.java.name)

    @JvmStatic
    fun isIndexingEnabledForBuild(build: SBuild): Boolean {
        if (!TeamCityProperties.getBooleanOrTrue(NuGetServerConstants.FEED_INDEXING_ENABLED_PROP)) {
            LOG.info("Skip NuGet metadata generation for build ${LogUtil.describe(build)}. NuGet packages indexing disabled on the server.")
            return false
        }

        val buildType = build.buildType ?: return true
        val indexingEnabled = buildType.configParameters[NuGetServerConstants.FEED_INDEXING_ENABLED_PROP]?.toBoolean() ?: true
        if (!indexingEnabled) {
            LOG.info("Skip NuGet metadata generation for build ${LogUtil.describe(build)}. NuGet packages indexing disabled for build type ${LogUtil.describe(buildType as BuildType)}.")
        }

        return indexingEnabled
    }
}
