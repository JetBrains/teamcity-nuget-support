package jetbrains.buildServer.nuget.feed.server.index

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation
import jetbrains.buildServer.nuget.feed.server.index.impl.PackagesIndexImpl
import jetbrains.buildServer.serverSide.metadata.MetadataStorage

class NuGetFeedFactoryImpl(private val serverSettings: NuGetServerSettings,
                           private val metadataStorage: MetadataStorage,
                           private val transformations: Collection<PackageTransformation>) : NuGetFeedFactory {
    override fun createFeed(feedData: NuGetFeedData): NuGetFeed {
        return NuGetFeed(PackagesIndexImpl(feedData, metadataStorage, transformations), serverSettings)
    }
}
