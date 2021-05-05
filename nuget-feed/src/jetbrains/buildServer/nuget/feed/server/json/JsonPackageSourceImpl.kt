package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes

class JsonPackageSourceImpl(private val nugetFeed: NuGetFeed) : JsonPackageSource {
    override fun getPackages(id: String, version: String): List<NuGetIndexEntry> {
        return nugetFeed.find(mapOf(
                NuGetPackageAttributes.ID to id,
                NuGetPackageAttributes.VERSION to version
        ), true)
    }

    override fun getPackages(id: String): List<NuGetIndexEntry> {
        return nugetFeed.findPackagesById(id, true)
    }

    override fun searchPackages(query: String?, prerelease: Boolean, includeSemVer2: Boolean): List<NuGetIndexEntry> {
        if (query.isNullOrEmpty()) {
            if (!prerelease) {
                return nugetFeed.find(mapOf(NuGetPackageAttributes.IS_PRERELEASE to false.toString()), includeSemVer2)
            } else {
                return nugetFeed.getAll(includeSemVer2)
            }
        }
        return nugetFeed.search(query, "", prerelease, includeSemVer2)
    }
}

