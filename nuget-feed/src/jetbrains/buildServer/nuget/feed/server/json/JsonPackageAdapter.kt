package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry

interface JsonPackageAdapter {
    fun createPackageResponse(entry: NuGetIndexEntry): JsonRegistrationPackageResponse

    fun createPackagesResponse(id: String, entries: List<NuGetIndexEntry>): JsonRegistrationResponse
}
