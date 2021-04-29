package jetbrains.buildServer.nuget.feed.server.json

class JsonPackageAdapterFactoryImpl: JsonPackageAdapterFactory {
    override fun create(context: JsonNuGetFeedContext): JsonPackageAdapter {
        return JsonPackageAdapterImpl(context)
    }
}
