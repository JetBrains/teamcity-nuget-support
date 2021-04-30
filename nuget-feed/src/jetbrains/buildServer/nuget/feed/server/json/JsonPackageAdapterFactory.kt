package jetbrains.buildServer.nuget.feed.server.json

interface JsonPackageAdapterFactory {
    fun create(context: JsonNuGetFeedContext): JsonPackageAdapter
}

