package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.agent.AgentRunningBuild
import java.io.File

interface NuGetPackageServiceFeedTransportProvider {
    @Throws(Exception::class)
    fun createTransport(build: AgentRunningBuild): NuGetPackageServiceFeedTransport
}

interface NuGetPackageServiceFeedTransport {
    @Throws(Exception::class)
    fun sendPackage(apiKey: String, file: File): NuGetPackageServiceFeedResponse
}

interface NuGetPackageServiceFeedResponse {
    val statusCode: Int
    val message: String
    val isSuccessful: Boolean
}
