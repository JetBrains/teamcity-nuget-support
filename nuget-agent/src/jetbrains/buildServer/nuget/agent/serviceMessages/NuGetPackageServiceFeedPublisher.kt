package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.nuget.common.index.NuGetPackageData

interface NuGetPackageServiceFeedPublisher {
    fun publishPackages(packages: Collection<NuGetPackageData>)
}
