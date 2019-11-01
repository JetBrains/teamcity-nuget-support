package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.nuget.common.index.NuGetPackageData

data class Metadata(val state: MetadataState, val packages: Collection<NuGetPackageData> = emptyList())
