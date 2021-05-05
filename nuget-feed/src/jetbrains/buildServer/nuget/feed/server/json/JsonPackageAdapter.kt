package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry

interface JsonPackageAdapter {
    fun createPackageResponse(entry: NuGetIndexEntry): JsonRegistrationPackageResponse

    fun createPackagesResponse(id: String, entries: List<NuGetIndexEntry>): JsonRegistrationResponse

    fun createSearchPackagesResponse(entries: List<NuGetIndexEntry>, take: Int, skip: Int?): JsonSearchResponse

    fun createPackageVersionsResponse(entries: List<NuGetIndexEntry>): JsonPackageVersions

    fun createDownloadContentUrl(entry: NuGetIndexEntry, extension: String): String?
}
