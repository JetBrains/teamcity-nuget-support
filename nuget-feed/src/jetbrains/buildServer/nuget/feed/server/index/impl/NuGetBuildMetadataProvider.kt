package jetbrains.buildServer.nuget.feed.server.index.impl

import jetbrains.buildServer.serverSide.SBuild

interface NuGetBuildMetadataProvider {
    fun getPackagesMetadata(build: SBuild): Metadata
}
