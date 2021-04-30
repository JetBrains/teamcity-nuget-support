package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry

interface JsonPackageSource {
    fun getPackages(id: String, version: String) : List<NuGetIndexEntry>

    fun getPackages(id: String) : List<NuGetIndexEntry>
}
