package jetbrains.buildServer.nuget.agent.serviceMessages

interface NuGetPackageServiceFeedUrlProvider {
    fun getUrl() : String
}
