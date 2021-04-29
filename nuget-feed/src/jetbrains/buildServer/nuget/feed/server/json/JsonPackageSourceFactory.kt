package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed

interface JsonPackageSourceFactory {
    fun create(nugetFeed: NuGetFeed): JsonPackageSource
}

