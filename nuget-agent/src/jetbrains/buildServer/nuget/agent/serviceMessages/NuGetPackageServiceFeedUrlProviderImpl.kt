package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.nuget.common.FeedConstants.*
import jetbrains.buildServer.util.EventDispatcher
import org.jetbrains.annotations.NotNull

class NuGetPackageServiceFeedUrlProviderImpl(
        @NotNull private val myConfiguration: BuildAgentConfiguration
) : NuGetPackageServiceFeedUrlProvider {

    override fun getUrl(build: AgentRunningBuild): String {
        val projectId = build.getSharedConfigParameters().get(ServerProvidedProperties.TEAMCITY_PROJECT_ID_PARAM)
        return myConfiguration.serverUrl + "/" + HTTP_AUTH + NUGET_PATH_PREFIX + NUGET_PROJECT_PATH_SUFFIX + NUGET_SERVICE_FEED_PATH_SUFFIX + "/" + projectId + "/"
    }

    companion object {
        const val HTTP_AUTH = "httpAuth"
    }
}
