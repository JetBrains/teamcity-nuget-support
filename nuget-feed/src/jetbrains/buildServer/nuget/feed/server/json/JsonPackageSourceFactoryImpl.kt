package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed

class JsonPackageSourceFactoryImpl : JsonPackageSourceFactory {
    override fun create(nugetFeed: NuGetFeed): JsonPackageSource {
        return JsonPackageSourceImpl(nugetFeed)
    }
}
