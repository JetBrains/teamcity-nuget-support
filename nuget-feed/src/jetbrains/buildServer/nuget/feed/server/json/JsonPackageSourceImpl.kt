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

    override fun getPackageNames(id: String, skip: Int?, take: Int, prerelease: Boolean, includeSemVer2: Boolean): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

