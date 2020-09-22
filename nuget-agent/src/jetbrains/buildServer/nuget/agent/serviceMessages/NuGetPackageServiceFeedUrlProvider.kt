package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.agent.AgentRunningBuild

interface NuGetPackageServiceFeedUrlProvider {
    fun getUrl(build: AgentRunningBuild): String
}
